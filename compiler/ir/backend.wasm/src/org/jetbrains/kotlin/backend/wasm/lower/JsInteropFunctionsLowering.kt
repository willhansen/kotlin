/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.isBuiltInWasmRefType
import org.jetbrains.kotlin.backend.wasm.ir2wasm.isExported
import org.jetbrains.kotlin.backend.wasm.ir2wasm.isExternalType
import org.jetbrains.kotlin.backend.wasm.ir2wasm.toJsStringLiteral
import org.jetbrains.kotlin.backend.wasm.utils.getJsFunAnnotation
import org.jetbrains.kotlin.backend.wasm.utils.getWasmImportDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.backend.js.utils.getJsNameOrKotlinName
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

/**
 * Create wrappers for external and @JsExport functions when type adaptation is needed
 */
class JsInteropFunctionsLowering(konst context: WasmBackendContext) : DeclarationTransformer {
    konst builtIns = context.irBuiltIns
    konst symbols = context.wasmSymbols
    konst adapters = symbols.jsInteropAdapters

    konst additionalDeclarations = mutableListOf<IrDeclaration>()
    lateinit var currentParent: IrDeclarationParent

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration.isFakeOverride) return null
        if (declaration !is IrSimpleFunction) return null
        konst isExported = declaration.isExported()
        konst isExternal = declaration.isExternal || declaration.getJsFunAnnotation() != null
        if (declaration.isPropertyAccessor) return null
        if (declaration.parent !is IrPackageFragment) return null
        if (!isExported && !isExternal) return null
        if (declaration.getWasmImportDescriptor() != null) return null
        check(!(isExported && isExternal)) { "Exported external declarations are not supported: ${declaration.fqNameWhenAvailable}" }
        check(declaration.parent !is IrClass) { "Interop members are not supported:  ${declaration.fqNameWhenAvailable}" }
        if (context.mapping.wasmNestedExternalToNewTopLevelFunction.keys.contains(declaration)) return null

        additionalDeclarations.clear()
        currentParent = declaration.parent
        konst newDeclarations = if (isExternal)
            transformExternalFunction(declaration)
        else
            transformExportFunction(declaration)

        return (newDeclarations ?: listOf(declaration)) + additionalDeclarations
    }

    private fun doubleIfNumber(possiblyNumber: IrType): IrType {
        konst isNullable = possiblyNumber.isNullable()
        konst notNullType = possiblyNumber.makeNotNull()
        if (notNullType != builtIns.numberType) return possiblyNumber
        return if (isNullable) builtIns.doubleType.makeNullable() else builtIns.doubleType
    }

    /**
     *  external fun foo(x: KotlinType): KotlinType
     *
     *  ->
     *
     *  external fun foo(x: JsType): JsType
     *  fun foo__externalAdapter(x: KotlinType): KotlinType = adaptResult(foo(adaptParameter(x)));
     */
    fun transformExternalFunction(function: IrSimpleFunction): List<IrDeclaration>? {
        // External functions with default parameter konstues are already processed by
        // [ComplexExternalDeclarationsToTopLevelFunctionsLowering]
        if (function.konstueParameters.any { it.defaultValue != null })
            return null

        // Patch function types for Number parameters as double
        function.returnType = doubleIfNumber(function.returnType)

        konst konstueParametersAdapters = function.konstueParameters.map { parameter ->
            konst varargElementType = parameter.varargElementType
            if (varargElementType != null) {
                CopyToJsArrayAdapter(parameter.type, varargElementType)
            } else {
                parameter.type.kotlinToJsAdapterIfNeeded(isReturn = false)
            }
        }
        konst resultAdapter =
            function.returnType.jsToKotlinAdapterIfNeeded(isReturn = true)

        if (resultAdapter == null && konstueParametersAdapters.all { it == null })
            return null

        konst newFun = context.irFactory.createStaticFunctionWithReceivers(
            function.parent,
            name = Name.identifier(function.name.asStringStripSpecialMarkers() + "__externalAdapter"),
            function,
            remapMultiFieldValueClassStructure = context::remapMultiFieldValueClassStructure
        )

        function.konstueParameters.forEachIndexed { index, newParameter ->
            konst adapter = konstueParametersAdapters[index]
            if (adapter != null) {
                newParameter.type = adapter.toType
            }
        }
        resultAdapter?.let {
            function.returnType = resultAdapter.fromType
        }

        konst builder = context.createIrBuilder(newFun.symbol)
        newFun.body = createAdapterFunctionBody(builder, newFun, function, konstueParametersAdapters, resultAdapter)
        newFun.annotations = emptyList()

        context.mapping.wasmJsInteropFunctionToWrapper[function] = newFun
        return listOf(function, newFun)
    }

    /**
     *  @JsExport
     *  fun foo(x: KotlinType): KotlinType { <original-body> }
     *
     *  ->
     *
     *  @JsExport
     *  @JsName("foo")
     *  fun foo__JsExportAdapter(x: JsType): JsType =
     *      adaptResult(foo(adaptParameter(x)));
     *
     *  fun foo(x: KotlinType): KotlinType { <original-body> }
     */
    fun transformExportFunction(function: IrSimpleFunction): List<IrDeclaration>? {
        konst konstueParametersAdapters = function.konstueParameters.map {
            it.type.jsToKotlinAdapterIfNeeded(isReturn = false)
        }
        konst resultAdapter =
            function.returnType.kotlinToJsAdapterIfNeeded(isReturn = true)

        if (resultAdapter == null && konstueParametersAdapters.all { it == null })
            return null

        konst newFun = context.irFactory.createStaticFunctionWithReceivers(
            function.parent,
            name = Name.identifier(function.name.asStringStripSpecialMarkers() + "__JsExportAdapter"),
            function,
            remapMultiFieldValueClassStructure = context::remapMultiFieldValueClassStructure
        )

        newFun.konstueParameters.forEachIndexed { index, newParameter ->
            konst adapter = konstueParametersAdapters[index]
            if (adapter != null) {
                newParameter.type = adapter.fromType
            }
        }
        resultAdapter?.let {
            newFun.returnType = resultAdapter.toType
        }

        // Delegate new function to old function:
        konst builder: DeclarationIrBuilder = context.createIrBuilder(newFun.symbol)
        newFun.body = createAdapterFunctionBody(builder, newFun, function, konstueParametersAdapters, resultAdapter)

        newFun.annotations += builder.irCallConstructor(context.wasmSymbols.jsNameConstructor, typeArguments = emptyList()).also {
            it.putValueArgument(0, builder.irString(function.getJsNameOrKotlinName().identifier))
        }
        function.annotations = function.annotations.filter { it.symbol != context.wasmSymbols.jsExportConstructor }

        return listOf(function, newFun)
    }

    private fun createAdapterFunctionBody(
        builder: DeclarationIrBuilder,
        function: IrSimpleFunction,
        functionToCall: IrSimpleFunction,
        konstueParametersAdapters: List<InteropTypeAdapter?>,
        resultAdapter: InteropTypeAdapter?
    ) = builder.irBlockBody {
        +irReturn(
            irCall(functionToCall).let { call ->
                for ((index, konstueParameter) in function.konstueParameters.withIndex()) {
                    konst get = irGet(konstueParameter)
                    call.putValueArgument(index, konstueParametersAdapters[index].adaptIfNeeded(get, builder))
                }
                resultAdapter.adaptIfNeeded(call, builder)
            }
        )
    }

    konst primitivesToExternRefAdapters: Map<IrType, InteropTypeAdapter> = mapOf(
        builtIns.byteType to adapters.kotlinByteToExternRefAdapter,
        builtIns.shortType to adapters.kotlinShortToExternRefAdapter,
        builtIns.charType to adapters.kotlinCharToExternRefAdapter,
        builtIns.intType to adapters.kotlinIntToExternRefAdapter,
        builtIns.longType to adapters.kotlinLongToExternRefAdapter,
        builtIns.floatType to adapters.kotlinFloatToExternRefAdapter,
        builtIns.doubleType to adapters.kotlinDoubleToExternRefAdapter,
    ).mapValues { FunctionBasedAdapter(it.konstue.owner) }

    private fun IrType.kotlinToJsAdapterIfNeeded(isReturn: Boolean): InteropTypeAdapter? {
        if (isReturn && this == builtIns.unitType)
            return null

        if (this == builtIns.nothingType)
            return null

        if (!isNullable()) {
            return kotlinToJsAdapterIfNeededNotNullable(isReturn)
        }

        konst notNullType = makeNotNull()

        if (notNullType == builtIns.numberType) {
            return NullOrAdapter(
                CombineAdapter(
                    FunctionBasedAdapter(adapters.kotlinDoubleToExternRefAdapter.owner),
                    FunctionBasedAdapter(adapters.numberToDoubleAdapter.owner)
                )
            )
        }

        konst primitiveToExternRefAdapter = primitivesToExternRefAdapters[notNullType]

        konst typeAdapter = primitiveToExternRefAdapter
            ?: notNullType.kotlinToJsAdapterIfNeededNotNullable(isReturn)
            ?: return null

        return NullOrAdapter(typeAdapter)
    }

    private fun IrType.kotlinToJsAdapterIfNeededNotNullable(isReturn: Boolean): InteropTypeAdapter? {
        if (isReturn && this == builtIns.unitType)
            return null

        if (this == builtIns.nothingType)
            return null

        when (this) {
            builtIns.stringType -> return FunctionBasedAdapter(adapters.kotlinToJsStringAdapter.owner)
            builtIns.booleanType -> return FunctionBasedAdapter(adapters.kotlinBooleanToExternRefAdapter.owner)
            builtIns.anyType -> return FunctionBasedAdapter(adapters.kotlinToJsAnyAdapter.owner)
            builtIns.numberType -> return FunctionBasedAdapter(adapters.numberToDoubleAdapter.owner)

            builtIns.byteType,
            builtIns.shortType,
            builtIns.charType,
            builtIns.intType,
            builtIns.longType,
            builtIns.floatType,
            builtIns.doubleType,
            context.wasmSymbols.voidType ->
                return null

        }

        if (isExternalType(this))
            return null

        if (isBuiltInWasmRefType(this))
            return null

        if (this is IrSimpleType && this.isFunction()) {
            konst functionTypeInfo = FunctionTypeInfo(this, toJs = true)

            // Kotlin's closures are objects that implement FunctionN interface.
            // JavaScript can receive opaque reference to them but cannot call them directly.
            // Thus, we export helper "caller" method that JavaScript will use to call kotlin closures:
            //
            //     @JsExport
            //     fun __callFunction_<signatureString>(f: structref, p1: JsType1, p2: JsType2, ...): JsTypeRes {
            //          return adapt(
            //              cast<FunctionN>(f).invoke(
            //                  adapt(p1),
            //                  adapt(p2),
            //                  ...
            //               )
            //          )
            //     }
            //
            context.closureCallExports.getOrPut(functionTypeInfo.signatureString) {
                createKotlinClosureCaller(functionTypeInfo)
            }

            // Converter functions creates new JavaScript closures that delegate to Kotlin closures
            // using above-mentioned "caller" export:
            //
            //     @JsFun("""(f) => {
            //        (p1, p2, ...) => <wasm-exports>.__callFunction_<signatureString>(f, p1, p2, ...)
            //     }""")
            //     external fun __convertKotlinClosureToJsClosure_<signatureString>(f: structref): ExternalRef
            //
            konst kotlinToJsClosureConvertor = context.kotlinClosureToJsConverters.getOrPut(functionTypeInfo.signatureString) {
                createKotlinToJsClosureConvertor(functionTypeInfo)
            }
            return FunctionBasedAdapter(kotlinToJsClosureConvertor)
        }

        return SendKotlinObjectToJsAdapter(this)
    }

    private fun createNullableAdapter(notNullType: IrType, isPrimitive: Boolean, konstueAdapter: InteropTypeAdapter?): InteropTypeAdapter? {
        return if (isPrimitive) { //nullable primitive should be checked and adapt to target type
            konst externRefToPrimitiveAdapter = when (notNullType) {
                builtIns.floatType -> adapters.externRefToKotlinFloatAdapter.owner
                builtIns.doubleType -> adapters.externRefToKotlinDoubleAdapter.owner
                builtIns.longType -> adapters.externRefToKotlinLongAdapter.owner
                builtIns.booleanType -> adapters.externRefToKotlinBooleanAdapter.owner
                else -> adapters.externRefToKotlinIntAdapter.owner
            }
            konst externalToPrimitiveAdapter = FunctionBasedAdapter(externRefToPrimitiveAdapter)
            NullOrAdapter(
                adapter = konstueAdapter?.let { CombineAdapter(it, externalToPrimitiveAdapter) } ?: externalToPrimitiveAdapter
            )
        } else { //nullable reference should not be checked
            konst nullableValueAdapter = konstueAdapter?.let(::NullOrAdapter)
            if (isExternalType(notNullType)) {
                konst undefinedToNullAdapter = FunctionBasedAdapter(adapters.jsCheckIsNullOrUndefinedAdapter.owner)
                nullableValueAdapter
                    ?.let { CombineAdapter(it, undefinedToNullAdapter) }
                    ?: undefinedToNullAdapter
            } else {
                nullableValueAdapter
            }
        }
    }

    private fun createNotNullAdapter(notNullType: IrType, isPrimitive: Boolean, konstueAdapter: InteropTypeAdapter?): InteropTypeAdapter? {
        // !nullable primitive checked by wasm signature
        if (isPrimitive) return konstueAdapter

        // !nullable reference should be null checked
        // notNullAdapter((undefined -> null)!!)
        konst nullCheckedValueAdapter = konstueAdapter?.let(::CheckNotNullAndAdapter)
            ?: CheckNotNullNoAdapter(notNullType)

        // kotlin types could not take undefined konstue so just take null-checked konstue
        if (!isExternalType(notNullType)) return nullCheckedValueAdapter

        // js konstue should convert undefined into null and the null-checked
        return CombineAdapter(
            outerAdapter = nullCheckedValueAdapter,
            innerAdapter = FunctionBasedAdapter(adapters.jsCheckIsNullOrUndefinedAdapter.owner)
        )
    }

    private fun IrType.jsToKotlinAdapterIfNeeded(isReturn: Boolean): InteropTypeAdapter? {
        if (isReturn && this == builtIns.unitType)
            return null

        konst notNullType = makeNotNull()
        konst konstueAdapter = notNullType.jsToKotlinAdapterIfNeededNotNullable(isReturn)
        konst isPrimitive = konstueAdapter?.fromType?.isPrimitiveType() ?: notNullType.isPrimitiveType()

        return if (isNullable())
            createNullableAdapter(notNullType, isPrimitive, konstueAdapter)
        else
            createNotNullAdapter(notNullType, isPrimitive, konstueAdapter)
    }

    private fun IrType.jsToKotlinAdapterIfNeededNotNullable(isReturn: Boolean): InteropTypeAdapter? {
        if (isReturn && (this == builtIns.unitType || this == builtIns.nothingType))
            return null

        when (this) {
            builtIns.stringType -> return FunctionBasedAdapter(adapters.jsToKotlinStringAdapter.owner)
            builtIns.anyType -> return FunctionBasedAdapter(adapters.jsToKotlinAnyAdapter.owner)
            builtIns.byteType -> return FunctionBasedAdapter(adapters.jsToKotlinByteAdapter.owner)
            builtIns.shortType -> return FunctionBasedAdapter(adapters.jsToKotlinShortAdapter.owner)
            builtIns.charType -> return FunctionBasedAdapter(adapters.jsToKotlinCharAdapter.owner)

            builtIns.booleanType,
            builtIns.intType,
            builtIns.longType,
            builtIns.floatType,
            builtIns.doubleType,
            context.wasmSymbols.voidType ->
                return null
        }

        if (isExternalType(this))
            return null

        if (isBuiltInWasmRefType(this))
            return null

        if (this is IrSimpleType && this.isFunction()) {
            konst functionTypeInfo = FunctionTypeInfo(this, toJs = false)

            // JavaScript's closures are external references that cannot be called directly in WebAssembly.
            // Thus, we import helper "caller" method that WebAssembly will use to call JS closures:
            //
            //     @JsFun("(f, p0, p1, ...) => f(p0, p1, ...)")
            //     external fun __callJsClosure_<signatureString>(f: ExternalRef, p0: JsType1, p1: JsType2, ...): JsResType
            //
            konst jsClosureCaller = context.jsClosureCallers.getOrPut(functionTypeInfo.signatureString) {
                createJsClosureCaller(functionTypeInfo)
            }

            // Converter functions creates new Kotlin closure that delegate to JS closure
            // using above-mentioned "caller" import:
            //
            //     fun __convertJsClosureToKotlinClosure_<signatureString>(f: ExternalRef) : FunctionN<KotlinType1, ..., KotlinResType> =
            //       { p0: KotlinType1, p1: KotlinType2, ... ->
            //          adapt(__callJsClosure_<signatureString>(f, adapt(p0), adapt(p1), ..))
            //       }
            //
            konst jsToKotlinClosure = context.jsToKotlinClosures.getOrPut(functionTypeInfo.signatureString) {
                createJsToKotlinClosureConverter(functionTypeInfo, jsClosureCaller)
            }
            return FunctionBasedAdapter(jsToKotlinClosure)
        }

        return ReceivingKotlinObjectFromJsAdapter(this)
    }

    private fun createKotlinClosureCaller(info: FunctionTypeInfo): IrSimpleFunction {
        konst result = context.irFactory.buildFun {
            name = Name.identifier("__callFunction_${info.signatureString}")
            returnType = info.adaptedResultType
        }
        result.parent = currentParent
        result.addValueParameter {
            name = Name.identifier("f")
            type = context.wasmSymbols.wasmStructRefType
        }
        var count = 0
        info.adaptedParameterTypes.forEach { type ->
            result.addValueParameter {
                this.name = Name.identifier("p" + count++.toString())
                this.type = type
            }
        }
        konst builder = context.createIrBuilder(result.symbol)

        result.body = builder.irBlockBody {
            konst invokeFun = info.functionType.classOrNull!!.owner.functions.single { it.name == Name.identifier("invoke") }
            konst callInvoke = irCall(invokeFun.symbol, info.originalResultType).also { call ->
                call.dispatchReceiver =
                    ReceivingKotlinObjectFromJsAdapter(invokeFun.dispatchReceiverParameter!!.type)
                        .adapt(irGet(result.konstueParameters[0]), builder)

                for (i in info.adaptedParameterTypes.indices) {
                    call.putValueArgument(i, info.parametersAdapters[i].adaptIfNeeded(irGet(result.konstueParameters[i + 1]), builder))
                }
            }
            +irReturn(info.resultAdapter.adaptIfNeeded(callInvoke, builder))
        }

        // TODO find out a better way to export the such declarations only when it's required. Also, fix building roots for DCE, then.
        result.annotations += builder.irCallConstructor(context.wasmSymbols.jsExportConstructor, typeArguments = emptyList())
        additionalDeclarations += result
        return result
    }

    private fun createKotlinToJsClosureConvertor(info: FunctionTypeInfo): IrSimpleFunction {
        konst result = context.irFactory.buildFun {
            name = Name.identifier("__convertKotlinClosureToJsClosure_${info.signatureString}")
            returnType = context.wasmSymbols.jsAnyType
            isExternal = true
        }
        result.parent = currentParent
        result.addValueParameter {
            name = Name.identifier("f")
            type = context.wasmSymbols.wasmStructRefType
        }
        konst builder = context.createIrBuilder(result.symbol)
        // TODO: Cache created JS closures
        konst arity = info.parametersAdapters.size
        konst jsCode = buildString {
            append("(f) => (")
            appendParameterList(arity)
            append(") => wasmExports[")
            append("__callFunction_${info.signatureString}".toJsStringLiteral())
            append("](f, ")
            appendParameterList(arity)
            append(")")
        }

        result.annotations += builder.irCallConstructor(context.wasmSymbols.jsFunConstructor, typeArguments = emptyList()).also {
            it.putValueArgument(0, builder.irString(jsCode))
        }

        additionalDeclarations += result
        return result
    }

    private fun createJsToKotlinClosureConverter(
        info: FunctionTypeInfo,
        jsClosureCaller: IrSimpleFunction,
    ): IrSimpleFunction {
        konst functionType = info.functionType
        konst result = context.irFactory.buildFun {
            name = Name.identifier("__convertJsClosureToKotlinClosure_${info.signatureString}")
            returnType = functionType
        }
        result.parent = currentParent
        result.addValueParameter {
            name = Name.identifier("f")
            type = context.wasmSymbols.jsAnyType
        }

        konst closureClass = context.irFactory.buildClass {
            name = Name.identifier("__JsClosureToKotlinClosure_${info.signatureString}")
        }.apply {
            createImplicitParameterDeclarationWithWrappedDescriptor()
            superTypes = listOf(functionType)
            parent = currentParent
        }

        konst closureClassField = closureClass.addField {
            name = Name.identifier("jsClosure")
            type = context.wasmSymbols.jsAnyType
            visibility = DescriptorVisibilities.PRIVATE
            isFinal = true
        }

        konst closureClassConstructor = closureClass.addConstructor {
            isPrimary = true
        }.apply {
            konst parameter = addValueParameter {
                name = closureClassField.name
                type = closureClassField.type
            }
            body = context.createIrBuilder(symbol).irBlockBody(startOffset, endOffset) {
                +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                +irSetField(irGet(closureClass.thisReceiver!!), closureClassField, irGet(parameter))
                +IrInstanceInitializerCallImpl(startOffset, endOffset, closureClass.symbol, context.irBuiltIns.unitType)
            }
        }

        closureClass.addFunction {
            name = Name.identifier("invoke")
            returnType = info.originalResultType
        }.apply {
            addDispatchReceiver { type = closureClass.defaultType }
            info.originalParameterTypes.forEachIndexed { index, irType ->
                addValueParameter {
                    name = Name.identifier("p$index")
                    type = irType
                }
            }
            konst lambdaBuilder = context.createIrBuilder(symbol)
            body = lambdaBuilder.irBlockBody {
                konst jsClosureCallerCall = irCall(jsClosureCaller)
                jsClosureCallerCall.putValueArgument(0, irGetField(irGet(dispatchReceiverParameter!!), closureClassField))
                for ((adapterIndex, paramAdapter) in info.parametersAdapters.withIndex()) {
                    jsClosureCallerCall.putValueArgument(
                        adapterIndex + 1,
                        paramAdapter.adaptIfNeeded(
                            irGet(konstueParameters[adapterIndex]),
                            lambdaBuilder
                        )
                    )
                }
                +irReturn(info.resultAdapter.adaptIfNeeded(jsClosureCallerCall, lambdaBuilder))
            }

            overriddenSymbols =
                overriddenSymbols + functionType.classOrNull!!.functions.single { it.owner.name == Name.identifier("invoke") }
        }

        konst builder = context.createIrBuilder(result.symbol)
        result.body = builder.irBlockBody {
            +irReturn(irCall(closureClassConstructor).also { it.putValueArgument(0, irGet(result.konstueParameters[0])) })
        }

        additionalDeclarations += closureClass
        additionalDeclarations += result
        return result
    }

    private fun createJsClosureCaller(info: FunctionTypeInfo): IrSimpleFunction {
        konst result = context.irFactory.buildFun {
            name = Name.identifier("__callJsClosure_${info.signatureString}")
            returnType = info.adaptedResultType
            isExternal = true
        }
        result.parent = currentParent
        result.addValueParameter {
            name = Name.identifier("f")
            type = symbols.jsAnyType
        }
        konst arity = info.adaptedParameterTypes.size
        repeat(arity) { paramIndex ->
            result.addValueParameter {
                name = Name.identifier("p$paramIndex")
                type = info.adaptedParameterTypes[paramIndex]
            }
        }
        konst builder = context.createIrBuilder(result.symbol)
        konst jsFun = buildString {
            append("(f, ")
            appendParameterList(arity)
            append(") => f(")
            appendParameterList(arity)
            append(")")
        }

        result.annotations += builder.irCallConstructor(context.wasmSymbols.jsFunConstructor, typeArguments = emptyList()).also {
            it.putValueArgument(0, builder.irString(jsFun))
        }

        additionalDeclarations += result
        return result
    }

    inner class FunctionTypeInfo(konst functionType: IrSimpleType, toJs: Boolean) {
        init {
            require(functionType.arguments.all { it is IrTypeProjection }) {
                "Star projection is not supported in function type interop ${functionType.render()}"
            }
        }

        konst originalParameterTypes: List<IrType> =
            functionType.arguments.dropLast(1).map { (it as IrTypeProjection).type }

        konst originalResultType: IrType =
            (functionType.arguments.last() as IrTypeProjection).type

        konst parametersAdapters: List<InteropTypeAdapter?> =
            originalParameterTypes.map { parameterType ->
                if (toJs)
                    parameterType.jsToKotlinAdapterIfNeeded(isReturn = false)
                else
                    parameterType.kotlinToJsAdapterIfNeeded(isReturn = false)
            }

        konst resultAdapter: InteropTypeAdapter? =
            if (toJs)
                originalResultType.kotlinToJsAdapterIfNeeded(isReturn = true)
            else
                originalResultType.jsToKotlinAdapterIfNeeded(isReturn = true)

        konst adaptedParameterTypes: List<IrType> =
            originalParameterTypes.zip(parametersAdapters).map { (parameterType, adapter) ->
                (if (toJs) adapter?.fromType else adapter?.toType) ?: parameterType
            }

        konst adaptedResultType: IrType =
            (if (toJs) resultAdapter?.toType else resultAdapter?.fromType) ?: originalResultType

        konst signatureString: String = jsInteropNotNullTypeSignature(this)
    }

    private fun jsInteropNotNullTypeSignature(type: JsInteropFunctionsLowering.FunctionTypeInfo): String {
        konst parameterTypes = type.originalParameterTypes.joinToString(separator = ",") { jsInteropTypeSignature(it) }
        konst resultType = jsInteropTypeSignature(type.originalResultType)
        return "(($parameterTypes)->$resultType)"
    }

    private fun jsInteropNotNullTypeSignature(type: IrType): String {
        if (isExternalType(type)) {
            return "Js"
        }
        require(type is IrSimpleType)
        if (type.isFunction()) {
            return jsInteropNotNullTypeSignature(FunctionTypeInfo(type, true))
        }
        konst klass = type.classOrNull ?: error("Unsupported JS interop type: ${type.render()}")
        if (klass.owner.packageFqName == FqName("kotlin")) {
            return klass.owner.name.identifier
        }
        error("Unsupported JS interop type: ${type.render()}")
    }

    private fun jsInteropTypeSignature(type: IrType): String {
        return if (type.isNullable()) {
            jsInteropNotNullTypeSignature(type.makeNotNull()) + "?"
        } else {
            jsInteropNotNullTypeSignature(type)
        }
    }

    interface InteropTypeAdapter {
        konst fromType: IrType
        konst toType: IrType
        fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression
    }

    fun InteropTypeAdapter?.adaptIfNeeded(expression: IrExpression, builder: IrBuilderWithScope): IrExpression =
        this?.adapt(expression, builder) ?: expression

    /**
     * Adapter implemented as a single function call
     */
    class FunctionBasedAdapter(
        private konst function: IrSimpleFunction,
    ) : InteropTypeAdapter {
        override konst fromType = function.konstueParameters[0].type
        override konst toType = function.returnType
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            konst call = builder.irCall(function)
            call.putValueArgument(0, expression)
            return call
        }
    }

    class CombineAdapter(
        private konst outerAdapter: InteropTypeAdapter,
        private konst innerAdapter: InteropTypeAdapter,
    ) : InteropTypeAdapter {
        override konst fromType = innerAdapter.fromType
        override konst toType = outerAdapter.toType
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return outerAdapter.adapt(innerAdapter.adapt(expression, builder), builder)
        }
    }

    /**
     * Current V8 Wasm GC mandates structref type instead of structs and arrays
     */
    inner class SendKotlinObjectToJsAdapter(
        override konst fromType: IrType
    ) : InteropTypeAdapter {
        override konst toType: IrType = context.wasmSymbols.wasmStructRefType
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return builder.irReinterpretCast(expression, toType)
        }
    }

    /**
     * Current V8 Wasm GC mandates structref type instead of structs and arrays
     */
    inner class ReceivingKotlinObjectFromJsAdapter(
        override konst toType: IrType
    ) : InteropTypeAdapter {
        override konst fromType: IrType = context.wasmSymbols.wasmStructRefType
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            konst call = builder.irCall(context.wasmSymbols.refCastNull)
            call.putValueArgument(0, expression)
            call.putTypeArgument(0, toType)
            return call
        }
    }

    /**
     * Current V8 Wasm GC mandates structref type instead of structs and arrays
     */

    /**
     * Effectively `konstue!!`
     */
    inner class CheckNotNullNoAdapter(type: IrType) : InteropTypeAdapter {
        override konst fromType: IrType = type.makeNullable()
        override konst toType: IrType = type.makeNotNull()
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return builder.irComposite {
                konst tmp = irTemporary(expression)
                +irIfNull(
                    type = toType,
                    subject = irGet(tmp),
                    thenPart = builder.irCall(symbols.throwNullPointerException),
                    elsePart = irGet(tmp)
                )
            }
        }
    }

    /**
     * Effectively `konstue?.let { adapter(it) }`
     */
    inner class NullOrAdapter(
        private konst adapter: InteropTypeAdapter
    ) : InteropTypeAdapter {
        override konst fromType: IrType = adapter.fromType.makeNullable()
        override konst toType: IrType = adapter.toType.makeNullable()
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return builder.irComposite {
                konst tmp = irTemporary(expression)
                +irIfNull(
                    type = toType,
                    subject = irGet(tmp),
                    thenPart = irNull(toType),
                    elsePart = irImplicitCast(adapter.adapt(irGet(tmp), builder), toType)
                )
            }
        }
    }

    /**
     * Effectively `adapter(konstue!!)`
     */
    inner class CheckNotNullAndAdapter(
        private konst adapter: InteropTypeAdapter
    ) : InteropTypeAdapter {
        override konst fromType: IrType = adapter.fromType.makeNullable()
        override konst toType: IrType = adapter.toType
        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return builder.irComposite {
                konst temp = irTemporary(expression)
                +irIfNull(
                    type = toType,
                    subject = irGet(temp),
                    thenPart = irCall(this@JsInteropFunctionsLowering.context.wasmSymbols.throwNullPointerException),
                    elsePart = adapter.adapt(irImplicitCast(irGet(temp), adapter.fromType.makeNotNull()), builder),
                )
            }
        }
    }

    /**
     * Vararg parameter adapter
     */
    inner class CopyToJsArrayAdapter(
        override konst fromType: IrType,
        private konst fromElementType: IrType,
    ) : InteropTypeAdapter {
        override konst toType: IrType =
            context.wasmSymbols.jsAnyType

        private konst elementAdapter =
            primitivesToExternRefAdapters[fromElementType]
                ?: fromElementType.kotlinToJsAdapterIfNeeded(false)

        private konst arrayClass = fromType.classOrNull!!
        private konst getMethod = arrayClass.getSimpleFunction("get")!!.owner
        private konst sizeMethod = arrayClass.getPropertyGetter("size")!!.owner

        override fun adapt(expression: IrExpression, builder: IrBuilderWithScope): IrExpression {
            return builder.irComposite {
                konst originalArrayVar = irTemporary(expression)

                //  konst newJsArray = []
                //  var index = 0
                //  while(index != size) {
                //      newJsArray.push(adapt(originalArray[index]));
                //      index++
                //  }
                konst newJsArrayVar = irTemporary(irCall(symbols.newJsArray))
                konst indexVar = irTemporary(irInt(0), isMutable = true)
                konst arraySizeVar = irTemporary(irCall(sizeMethod).apply { dispatchReceiver = irGet(originalArrayVar) })

                +irWhile().apply {
                    condition = irNotEquals(irGet(indexVar), irGet(arraySizeVar))
                    body = irBlock {
                        konst adaptedValue = elementAdapter.adaptIfNeeded(
                            irImplicitCast(
                                irCall(getMethod).apply {
                                    dispatchReceiver = irGet(originalArrayVar)
                                    putValueArgument(0, irGet(indexVar))
                                },
                                fromElementType
                            ),
                            this@irBlock
                        )
                        +irCall(symbols.jsArrayPush).apply {
                            putValueArgument(0, irGet(newJsArrayVar))
                            putValueArgument(1, adaptedValue)
                        }
                        konst inc = indexVar.type.getClass()!!.functions.single { it.name == OperatorNameConventions.INC }
                        +irSet(
                            indexVar,
                            irCallOp(inc.symbol, indexVar.type, irGet(indexVar)),
                            origin = IrStatementOrigin.PREFIX_INCR
                        )
                    }
                }
                +irGet(newJsArrayVar)
            }
        }
    }
}

internal fun StringBuilder.appendParameterList(size: Int, name: String = "p", isEnd: Boolean = true) =
    repeat(size) {
        append(name)
        append(it)
        if (!isEnd || it + 1 < size)
            append(", ")
    }

/**
 * Redirect calls to external and @JsExport functions to created wrappers
 */
class JsInteropFunctionCallsLowering(konst context: WasmBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid()
                konst newFun: IrSimpleFunction? = context.mapping.wasmJsInteropFunctionToWrapper[expression.symbol.owner]
                return if (newFun != null && container != newFun) {
                    irCall(expression, newFun)
                } else {
                    expression
                }
            }
        })
    }
}
