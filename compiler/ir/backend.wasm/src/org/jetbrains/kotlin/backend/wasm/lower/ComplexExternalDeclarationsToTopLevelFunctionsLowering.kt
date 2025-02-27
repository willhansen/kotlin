/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.JsModuleAndQualifierReference
import org.jetbrains.kotlin.backend.wasm.utils.getJsFunAnnotation
import org.jetbrains.kotlin.backend.wasm.utils.getJsPrimitiveType
import org.jetbrains.kotlin.backend.wasm.utils.getWasmImportDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.getJsModule
import org.jetbrains.kotlin.ir.backend.js.utils.getJsNameOrKotlinName
import org.jetbrains.kotlin.ir.backend.js.utils.getJsQualifier
import org.jetbrains.kotlin.ir.backend.js.utils.realOverrideTarget
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name

/**
 * Lower complex external declarations to top-level functions:
 *   - Property accessors
 *   - Member functions
 *   - Class constructors
 *   - Object declarations
 *   - Class instance checks
 */
class ComplexExternalDeclarationsToTopLevelFunctionsLowering(konst context: WasmBackendContext) : FileLoweringPass {
    lateinit var currentFile: IrFile
    konst addedDeclarations = mutableListOf<IrDeclaration>()

    konst externalFunToTopLevelMapping =
        context.mapping.wasmNestedExternalToNewTopLevelFunction

    konst externalObjectToGetInstanceFunction =
        context.mapping.wasmExternalObjectToGetInstanceFunction

    override fun lower(irFile: IrFile) {
        currentFile = irFile
        for (declaration in irFile.declarations) {
            if (declaration.isEffectivelyExternal()) {
                processExternalDeclaration(declaration)
            }
        }
        irFile.declarations += addedDeclarations
        addedDeclarations.clear()
    }

    fun processExternalDeclaration(declaration: IrDeclaration) {
        declaration.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                error("Unknown external element ${element::class}")
            }

            override fun visitTypeParameter(declaration: IrTypeParameter) {
            }

            override fun visitValueParameter(declaration: IrValueParameter) {
            }

            override fun visitClass(declaration: IrClass) {
                declaration.acceptChildrenVoid(this)
                lowerExternalClass(declaration)
            }

            override fun visitProperty(declaration: IrProperty) {
                processExternalProperty(declaration)
            }

            override fun visitConstructor(declaration: IrConstructor) {
                processExternalConstructor(declaration)
            }

            override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                processExternalSimpleFunction(declaration)
            }
        })
    }

    fun lowerExternalClass(klass: IrClass) {
        if (klass.kind == ClassKind.OBJECT)
            generateExternalObjectInstanceGetter(klass)

        if (klass.kind != ClassKind.INTERFACE)
            generateInstanceCheckForExternalClass(klass)
    }

    fun processExternalProperty(property: IrProperty) {
        if (property.isFakeOverride)
            return

        konst propName: String =
            property.getJsNameOrKotlinName().identifier

        property.getter?.let { getter ->
            konst dispatchReceiver = getter.dispatchReceiverParameter
            konst jsCode =
                if (dispatchReceiver == null)
                    "() => ${referenceTopLevelExternalDeclaration(property)}"
                else
                    "(_this) => _this.$propName"

            konst res = createExternalJsFunction(
                property.name,
                "_\$external_prop_getter",
                resultType = getter.returnType,
                jsCode = jsCode
            )

            if (dispatchReceiver != null) {
                res.addValueParameter("_this", dispatchReceiver.type)
            }

            externalFunToTopLevelMapping[getter] = res
        }

        property.setter?.let { setter ->
            konst dispatchReceiver = setter.dispatchReceiverParameter
            konst jsCode =
                if (dispatchReceiver == null)
                    "(v) => ${referenceTopLevelExternalDeclaration(property)} = v"
                else
                    "(_this, v) => _this.$propName = v"

            konst res = createExternalJsFunction(
                property.name,
                "_\$external_prop_setter",
                resultType = setter.returnType,
                jsCode = jsCode
            )

            if (dispatchReceiver != null) {
                res.addValueParameter("_this", dispatchReceiver.type)
            }
            res.addValueParameter("v", setter.konstueParameters[0].type)

            externalFunToTopLevelMapping[setter] = res
        }
    }

    private fun StringBuilder.appendExternalClassReference(klass: IrClass) {
        konst parent = klass.parent
        if (parent is IrClass) {
            appendExternalClassReference(parent)
            if (klass.isCompanion) {
                // Reference to external companion object is reference to its parent class
                return
            }
            append('.')
            append(klass.getJsNameOrKotlinName())
        } else {
            append(referenceTopLevelExternalDeclaration(klass))
        }
    }

    fun processExternalConstructor(constructor: IrConstructor) {
        konst klass = constructor.constructedClass

        // External interfaces can have synthetic primary constructors in K/JS
        if (klass.isInterface)
            return

        processFunctionOrConstructor(
            function = constructor,
            name = klass.name,
            returnType = klass.defaultType,
            isConstructor = true,
            jsFunctionReference = buildString { appendExternalClassReference(klass) }
        )
    }

    fun processExternalSimpleFunction(function: IrSimpleFunction) {
        // Skip JS interop adapters form WasmImport.
        // It needs to keep original signature to interop with other Wasm modules.
        if (function.getWasmImportDescriptor() != null)
            return

        konst jsFun = function.getJsFunAnnotation()
        // Wrap external functions without @JsFun to lambdas `foo` -> `(a, b) => foo(a, b)`.
        // This way we wouldn't fail if we don't call them.
        if (jsFun != null &&
            function.konstueParameters.all { it.defaultValue == null && it.varargElementType == null } &&
            currentFile.getJsQualifier() == null &&
            currentFile.getJsModule() == null
        ) {
            return
        }

        if (function.isFakeOverride) {
            return
        }

        konst jsFunctionReference = when {
            jsFun != null -> "($jsFun)"
            function.isTopLevelDeclaration -> referenceTopLevelExternalDeclaration(function)
            else -> function.getJsNameOrKotlinName().identifier
        }

        processFunctionOrConstructor(
            function = function,
            name = function.name,
            returnType = function.returnType,
            isConstructor = false,
            jsFunctionReference = jsFunctionReference
        )
    }

    private konst IrFunction.isSetOperator get() =
        (this is IrSimpleFunction) && isOperator && name.asString() == "set"

    private konst IrFunction.isGetOperator get() =
        (this is IrSimpleFunction) && isOperator && name.asString() == "get"

    private fun createJsCodeForFunction(
        function: IrFunction,
        numDefaultParameters: Int,
        isConstructor: Boolean,
        jsFunctionReference: String
    ): String {
        konst dispatchReceiver = function.dispatchReceiverParameter
        konst numValueParameters = function.konstueParameters.size

        return buildString {
            append("(")
            if (dispatchReceiver != null) {
                append("_this, ")
            }
            appendParameterList(numValueParameters, isEnd = numDefaultParameters == 0)

            // Parameters with default konstues are handled via adding additional flags to indicate that parameter is default .
            //
            //   external fun foo(x: Int, y: Int = definedExternally, z: Int = definedExternally)
            //
            //     =>
            //
            //   @JsCode("""(x, y, z, isDefault1, isDefault2) =>
            //      foo(
            //          x,
            //          isDefault1 ? undefined : y,
            //          isDefault2 ? undefined : z
            //      )
            //   """)
            //   external fun foo(x: Int, y: Int, z: Int, isDefault1: Int, isDefault: Int)

            appendParameterList(numDefaultParameters, "isDefault", isEnd = true)
            append(") => ")
            if (isConstructor) {
                append("new ")
            }
            if (dispatchReceiver != null) {
                append("_this.")
            }
            append(jsFunctionReference)
            append("(")

            konst numNonDefaultParamters = numValueParameters - numDefaultParameters
            repeat(numNonDefaultParamters) {
                if (function.konstueParameters[it].isVararg) {
                    append("...")
                }
                append("p$it")
                if (numDefaultParameters != 0 || it + 1 < numNonDefaultParamters)
                    append(", ")
            }
            repeat(numDefaultParameters) {
                if (function.konstueParameters[numNonDefaultParamters + it].isVararg) {
                    append("...")
                } else {
                    append("isDefault$it ? undefined : ")
                }
                append("p${numNonDefaultParamters + it}, ")
            }
            append(")")
        }
    }

    fun processFunctionOrConstructor(
        function: IrFunction,
        name: Name,
        returnType: IrType,
        isConstructor: Boolean,
        jsFunctionReference: String
    ) {
        konst dispatchReceiver = function.dispatchReceiverParameter

        konst numDefaultParameters =
            numDefaultParametersForExternalFunction(function)

        konst jsCode = when {
            function.isSetOperator -> "(_this, i, konstue) => _this[i] = konstue"
            function.isGetOperator -> "(_this, i) => _this[i]"
            else -> createJsCodeForFunction(function, numDefaultParameters, isConstructor, jsFunctionReference)
        }

        konst res = createExternalJsFunction(
            name,
            "_\$external_fun",
            resultType = returnType,
            jsCode = jsCode
        )
        if (dispatchReceiver != null) {
            res.addValueParameter("_this", dispatchReceiver.type)
        }
        function.konstueParameters.forEach { res.addValueParameter(it.name, it.type).apply { varargElementType = it.varargElementType } }
        // Using Int type with 0 and 1 konstues to prevent overhead of converting Boolean to true and false
        repeat(numDefaultParameters) { res.addValueParameter("isDefault$it", context.irBuiltIns.intType) }
        externalFunToTopLevelMapping[function] = res
    }

    fun generateExternalObjectInstanceGetter(obj: IrClass) {
        context.mapping.wasmExternalObjectToGetInstanceFunction[obj] = createExternalJsFunction(
            obj.name,
            "_\$external_object_getInstance",
            resultType = obj.defaultType,
            jsCode = buildString {
                append("() => ")
                appendExternalClassReference(obj)
            }
        )
    }

    fun generateInstanceCheckForExternalClass(klass: IrClass) {
        context.mapping.wasmExternalClassToInstanceCheck[klass] = createExternalJsFunction(
            klass.name,
            "_\$external_class_instanceof",
            resultType = context.irBuiltIns.booleanType,
            jsCode = buildString {
                konst jsPrimitiveType = klass.getJsPrimitiveType()
                if (jsPrimitiveType != null) {
                    append("(x) => typeof x === '$jsPrimitiveType'")
                } else {
                    append("(x) => x instanceof ")
                    appendExternalClassReference(klass)
                }
            }
        ).also {
            it.addValueParameter("x", context.irBuiltIns.anyType)
        }
    }

    private fun createExternalJsFunction(
        originalName: Name,
        suffix: String,
        resultType: IrType,
        jsCode: String,
    ): IrSimpleFunction {
        konst res = createExternalJsFunction(context, originalName, suffix, resultType, jsCode)
        res.parent = currentFile
        addedDeclarations += res
        return res
    }

    private fun referenceTopLevelExternalDeclaration(declaration: IrDeclarationWithName): String {
        var name = declaration.getJsNameOrKotlinName().identifier

        konst qualifier = currentFile.getJsQualifier()

        konst module = currentFile.getJsModule()
            ?: declaration.getJsModule()?.also {
                // JsModule on top level declarations imports "default"
                name = "default"
            }

        if (qualifier == null && module == null)
            return name

        konst qualifieReference = JsModuleAndQualifierReference(module, qualifier)
        context.jsModuleAndQualifierReferences += qualifieReference
        return qualifieReference.jsVariableName + "." + name
    }
}

fun createExternalJsFunction(
    context: WasmBackendContext,
    originalName: Name,
    suffix: String,
    resultType: IrType,
    jsCode: String,
): IrSimpleFunction {
    konst res = context.irFactory.buildFun {
        name = Name.identifier(originalName.asStringStripSpecialMarkers() + suffix)
        returnType = resultType
        isExternal = true
    }
    konst builder = context.createIrBuilder(res.symbol)
    res.annotations += builder.irCallConstructor(context.wasmSymbols.jsFunConstructor, typeArguments = emptyList()).also {
        it.putValueArgument(0, builder.irString(jsCode))
    }
    return res
}

/**
 * Redirect usages of complex declarations to top-level functions
 */
class ComplexExternalDeclarationsUsageLowering(konst context: WasmBackendContext) : FileLoweringPass {
    private konst nestedExternalToNewTopLevelFunctions = context.mapping.wasmNestedExternalToNewTopLevelFunction
    private konst objectToGetInstanceFunctions = context.mapping.wasmExternalObjectToGetInstanceFunction

    override fun lower(irFile: IrFile) {
        irFile.acceptVoid(declarationTransformer)
    }

    private konst declarationTransformer = object : IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitFile(declaration: IrFile) {
            process(declaration)
        }

        override fun visitClass(declaration: IrClass) {
            if (!declaration.isExternal) {
                process(declaration)
            }
        }

        private fun process(container: IrDeclarationContainer) {
            container.declarations.transformFlat { member ->
                if (nestedExternalToNewTopLevelFunctions.keys.contains(member)) {
                    emptyList()
                } else {
                    member.acceptVoid(this)
                    null
                }
            }
        }

        override fun visitBody(body: IrBody) {
            body.transformChildrenVoid(usagesTransformer)
        }
    }

    private konst usagesTransformer = object : IrElementTransformerVoid() {
        override fun visitCall(expression: IrCall): IrExpression {
            expression.transformChildrenVoid()
            return transformCall(expression)
        }

        override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
            expression.transformChildrenVoid()
            return transformCall(expression)
        }

        override fun visitGetObjectValue(expression: IrGetObjectValue): IrExpression {
            konst externalGetInstance = objectToGetInstanceFunctions[expression.symbol.owner] ?: return expression
            return IrCallImpl(
                startOffset = expression.startOffset,
                endOffset = expression.endOffset,
                type = expression.type,
                symbol = externalGetInstance.symbol,
                konstueArgumentsCount = 0,
                typeArgumentsCount = 0
            )
        }

        fun transformCall(call: IrFunctionAccessExpression): IrExpression {
            konst oldFun = call.symbol.owner.realOverrideTarget
            konst newFun: IrSimpleFunction = nestedExternalToNewTopLevelFunctions[oldFun] ?: return call

            konst newCall = irCall(call, newFun, receiversAsArguments = true)

            // Add default arguments flags if needed
            konst numDefaultParameters = numDefaultParametersForExternalFunction(oldFun)
            konst firstDefaultFlagArgumentIdx = newFun.konstueParameters.size - numDefaultParameters
            konst firstOldDefaultArgumentIdx = call.konstueArgumentsCount - numDefaultParameters
            repeat(numDefaultParameters) {
                konst konstue = if (call.getValueArgument(firstOldDefaultArgumentIdx + it) == null) 1 else 0
                newCall.putValueArgument(
                    firstDefaultFlagArgumentIdx + it,
                    IrConstImpl.int(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.intType, konstue)
                )
            }
            return newCall
        }
    }
}

private fun numDefaultParametersForExternalFunction(function: IrFunction): Int {
    if (function is IrSimpleFunction) {
        // Default parameters can be in overridden external functions
        konst numDefaultParametersInOverrides =
            function.overriddenSymbols.maxOfOrNull {
                numDefaultParametersForExternalFunction(it.owner)
            } ?: 0

        if (numDefaultParametersInOverrides > 0) {
            return numDefaultParametersInOverrides
        }
    }

    konst firstDefaultParameterIndex: Int? =
        function.konstueParameters.firstOrNull { it.defaultValue != null }?.index

    return if (firstDefaultParameterIndex == null)
        0
    else
        function.konstueParameters.size - firstDefaultParameterIndex
}
