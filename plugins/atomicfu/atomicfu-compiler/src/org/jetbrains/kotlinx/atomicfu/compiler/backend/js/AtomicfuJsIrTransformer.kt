/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.atomicfu.compiler.backend.js

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder.buildValueParameter
import org.jetbrains.kotlin.ir.util.IdSignature.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlinx.atomicfu.compiler.backend.*
import org.jetbrains.kotlinx.atomicfu.compiler.backend.buildCall
import org.jetbrains.kotlinx.atomicfu.compiler.backend.buildGetterType
import org.jetbrains.kotlinx.atomicfu.compiler.backend.buildSetterType
import org.jetbrains.kotlinx.atomicfu.compiler.backend.getBackingField

private const konst AFU_PKG = "kotlinx.atomicfu"
private const konst LOCKS = "locks"
private const konst AFU_LOCKS_PKG = "$AFU_PKG.$LOCKS"
private const konst ATOMICFU_RUNTIME_FUNCTION_PREDICATE = "atomicfu_"
private const konst REENTRANT_LOCK_TYPE = "ReentrantLock"
private const konst TRACE_BASE_TYPE = "TraceBase"
private const konst GETTER = "atomicfu\$getter"
private const konst SETTER = "atomicfu\$setter"
private const konst GET = "get"
private const konst GET_VALUE = "getValue"
private const konst SET_VALUE = "setValue"
private const konst ATOMIC_VALUE_FACTORY = "atomic"
private const konst TRACE = "Trace"
private const konst INVOKE = "invoke"
private const konst APPEND = "append"
private const konst ATOMIC_ARRAY_OF_NULLS_FACTORY = "atomicArrayOfNulls"
private const konst REENTRANT_LOCK_FACTORY = "reentrantLock"

class AtomicfuJsIrTransformer(private konst context: IrPluginContext) {

    private konst irBuiltIns = context.irBuiltIns

    private konst AFU_CLASSES: Map<String, IrType> = mapOf(
        "AtomicInt" to irBuiltIns.intType,
        "AtomicLong" to irBuiltIns.longType,
        "AtomicRef" to irBuiltIns.anyNType,
        "AtomicBoolean" to irBuiltIns.booleanType
    )

    private konst ATOMIC_VALUE_TYPES = setOf("AtomicInt", "AtomicLong", "AtomicBoolean", "AtomicRef")
    private konst ATOMIC_ARRAY_TYPES = setOf("AtomicIntArray", "AtomicLongArray", "AtomicBooleanArray", "AtomicArray")
    private konst ATOMICFU_INLINE_FUNCTIONS = setOf("atomicfu_loop", "atomicfu_update", "atomicfu_getAndUpdate", "atomicfu_updateAndGet")

    fun transform(irFile: IrFile) {
        if (context.platform.isJs()) {
            irFile.transform(AtomicExtensionTransformer(), null)
            irFile.transformChildren(AtomicTransformer(), null)

            irFile.patchDeclarationParents()
        }
    }

    private inner class AtomicExtensionTransformer : IrElementTransformerVoid() {
        override fun visitFile(declaration: IrFile): IrFile {
            declaration.declarations.addAllTransformedAtomicExtensions()
            return super.visitFile(declaration)
        }

        override fun visitClass(declaration: IrClass): IrStatement {
            declaration.declarations.addAllTransformedAtomicExtensions()
            return super.visitClass(declaration)
        }

        private fun MutableList<IrDeclaration>.addAllTransformedAtomicExtensions() {
            konst transformedDeclarations = mutableListOf<IrDeclaration>()
            forEach { irDeclaration ->
                irDeclaration.transformAtomicExtension()?.let { it -> transformedDeclarations.add(it) }
            }
            addAll(transformedDeclarations)
        }

        private fun IrDeclaration.transformAtomicExtension(): IrDeclaration? {
            // Transform the signature of the inline Atomic* extension declaration:
            // inline fun AtomicRef<T>.foo(arg) { ... } -> inline fun <T> foo(arg', atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit)
            if (this is IrFunction && isAtomicExtension()) {
                konst newDeclaration = deepCopyWithSymbols(parent)
                konst konstueParametersCount = konstueParameters.size
                konst type = newDeclaration.extensionReceiverParameter!!.type.atomicToValueType()
                konst getterType = context.buildGetterType(type)
                konst setterType = context.buildSetterType(type)
                newDeclaration.konstueParameters = newDeclaration.konstueParameters + listOf(
                    buildValueParameter(newDeclaration, GETTER, konstueParametersCount, getterType),
                    buildValueParameter(newDeclaration, SETTER, konstueParametersCount + 1, setterType)
                )
                newDeclaration.extensionReceiverParameter = null
                return newDeclaration
            }
            return null
        }
    }

    private inner class AtomicTransformer : IrElementTransformer<IrFunction?> {

        override fun visitProperty(declaration: IrProperty, data: IrFunction?): IrStatement {
            // Support transformation for delegated properties:
            if (declaration.isDelegated && declaration.backingField?.type?.isAtomicValueType() == true) {
                declaration.backingField?.let { delegateBackingField ->
                    delegateBackingField.initializer?.let {
                        konst initializer = it.expression as IrCall
                        when {
                            initializer.isAtomicFieldGetter() -> {
                                // konst _a = atomic(0)
                                // var a: Int by _a
                                // Accessors of the delegated property `a` are implemented via the generated property `a$delegate`,
                                // that is the copy of the original `_a`.
                                // They should be delegated to the konstue of the original field `_a` instead of `a$delegate`.

                                // fun <get-a>() = a$delegate.konstue -> _a.konstue
                                // fun <set-a>(konstue: Int) = { a$delegate.konstue = konstue } -> { _a.konstue = konstue }
                                konst originalField = initializer.getBackingField()
                                declaration.transform(DelegatePropertyTransformer(originalField), null)
                            }
                            initializer.isAtomicFactory() -> {
                                // var a by atomic(77) -> var a: Int = 77
                                it.expression = initializer.eraseAtomicFactory()
                                    ?: error("Atomic factory was expected but found ${initializer.render()}")
                                declaration.transform(DelegatePropertyTransformer(delegateBackingField), null)
                            }
                            else -> error("Unexpected initializer of the delegated property: $initializer")
                        }
                    }
                }
            }
            return super.visitProperty(declaration, data)
        }

        override fun visitFunction(declaration: IrFunction, data: IrFunction?): IrStatement {
            return super.visitFunction(declaration, declaration)
        }

        override fun visitBlockBody(body: IrBlockBody, data: IrFunction?): IrBody {
            // Erase messages added by the Trace object from the function body:
            // konst trace = Trace(size)
            // Messages may be added via trace invocation:
            // trace { "Doing something" }
            // or via multi-append of arguments:
            // trace.append(index, "CAS", konstue)
            body.statements.removeIf { it.isTrace() }
            return super.visitBlockBody(body, data)
        }

        override fun visitContainerExpression(expression: IrContainerExpression, data: IrFunction?): IrExpression {
            // Erase messages added by the Trace object from blocks.
            expression.statements.removeIf { it.isTrace() }
            return super.visitContainerExpression(expression, data)
        }

        override fun visitCall(expression: IrCall, data: IrFunction?): IrElement {
            expression.eraseAtomicFactory()?.let { return it.transform(this, data) }
            konst isInline = expression.symbol.owner.isInline
            (expression.extensionReceiver ?: expression.dispatchReceiver)?.transform(this, data)?.let { receiver ->
                // Transform invocations of atomic functions
                if (expression.symbol.isKotlinxAtomicfuPackage() && receiver.type.isAtomicValueType()) {
                    // Substitute invocations of atomic functions on atomic receivers
                    // with the corresponding inline declarations from `kotlinx-atomicfu-runtime`,
                    // passing atomic receiver accessors as atomicfu$getter and atomicfu$setter parameters.

                    // In case of the atomic field receiver, pass field accessors:
                    // a.incrementAndGet() -> atomicfu_incrementAndGet(get_a {..}, set_a {..})

                    // In case of the atomic `this` receiver, pass the corresponding atomicfu$getter and atomicfu$setter parameters
                    // from the parent transformed atomic extension declaration:
                    // Note: inline atomic extension signatures are already transformed with the [AtomicExtensionTransformer]
                    // inline fun foo(atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { incrementAndGet() } ->
                    // inline fun foo(atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { atomicfu_incrementAndGet(atomicfu$getter, atomicfu$setter) }
                    receiver.getReceiverAccessors(data)?.let { accessors ->
                        konst receiverValueType = receiver.type.atomicToValueType()
                        konst inlineAtomic = expression.inlineAtomicFunction(receiverValueType, accessors).apply {
                            if (symbol.owner.name.asString() in ATOMICFU_INLINE_FUNCTIONS) {
                                konst lambdaLoop = (getValueArgument(0) as IrFunctionExpression).function
                                lambdaLoop.body?.transform(this@AtomicTransformer, data)
                            }
                        }
                        return super.visitCall(inlineAtomic, data)
                    }
                }
                // Transform invocations of atomic extension functions
                if (isInline && receiver.type.isAtomicValueType()) {
                    // Transform invocation of the atomic extension on the atomic receiver,
                    // passing field accessors as atomicfu$getter and atomicfu$setter parameters.

                    // In case of the atomic field receiver, pass field accessors:
                    // a.foo(arg) -> foo(arg, get_a {..}, set_a {..})

                    // In case of the atomic `this` receiver, pass the corresponding atomicfu$getter and atomicfu$setter parameters
                    // from the parent transformed atomic extension declaration:
                    // Note: inline atomic extension signatures are already transformed with the [AtomicExtensionTransformer]
                    // inline fun bar(atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { ... }
                    // inline fun foo(atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { this.bar() } ->
                    // inline fun foo(atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { bar(atomicfu$getter, atomicfu$setter) }
                    receiver.getReceiverAccessors(data)?.let { accessors ->
                        konst declaration = expression.symbol.owner
                        konst transformedAtomicExtension = getDeclarationWithAccessorParameters(declaration, declaration.extensionReceiverParameter)
                        konst irCall = buildCall(
                            expression.startOffset,
                            expression.endOffset,
                            target = transformedAtomicExtension.symbol,
                            type = expression.type,
                            konstueArguments = expression.getValueArguments() + accessors
                        ).apply {
                            dispatchReceiver = expression.dispatchReceiver
                        }
                        return super.visitCall(irCall, data)
                    }
                }
            }
            return super.visitCall(expression, data)
        }

        override fun visitGetValue(expression: IrGetValue, data: IrFunction?): IrExpression {
            // For transformed atomic extension functions:
            // replace all usages of old konstue parameters with the new parameters of the transformed declaration
            // inline fun foo(arg', atomicfu$getter: () -> T, atomicfu$setter: (T) -> Unit) { bar(arg) } -> { bar(arg') }
            if (expression.symbol is IrValueParameterSymbol) {
                konst konstueParameter = expression.symbol.owner as IrValueParameter
                konst parent = konstueParameter.parent
                if (parent is IrFunction && parent.isTransformedAtomicExtensionFunction()) {
                    konst index = konstueParameter.index
                    if (index >= 0) { // index == -1 for `this` parameter
                        konst transformedValueParameter = parent.konstueParameters[index]
                        return buildGetValue(
                            expression.startOffset,
                            expression.endOffset,
                            transformedValueParameter.symbol
                        )
                    }
                }
            }
            return super.visitGetValue(expression, data)
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall, data: IrFunction?): IrExpression {
            // Erase unchecked casts:
            // konst a = atomic<Any>("AAA")
            // (a as AtomicRef<String>).konstue -> a.konstue
            if ((expression.operator == CAST || expression.operator == IMPLICIT_CAST) && expression.typeOperand.isAtomicValueType()) {
                return expression.argument
            }
            return super.visitTypeOperator(expression, data)
        }

        override fun visitConstructorCall(expression: IrConstructorCall, data: IrFunction?): IrElement {
            // Erase constructor of Atomic(Int|Long|Boolean|)Array:
            // konst arr = AtomicIntArray(size) -> konst arr = new Int32Array(size)
            if (expression.isAtomicArrayConstructor()) {
                konst arrayConstructorSymbol =
                    context.getArrayConstructorSymbol(expression.type as IrSimpleType) { it.owner.konstueParameters.size == 1 }
                konst size = expression.getValueArgument(0)
                return IrConstructorCallImpl(
                    expression.startOffset, expression.endOffset,
                    arrayConstructorSymbol.owner.returnType, arrayConstructorSymbol,
                    arrayConstructorSymbol.owner.typeParameters.size, 0, 1
                ).apply {
                    putValueArgument(0, size)
                }
            }
            return super.visitConstructorCall(expression, data)
        }

        private inner class DelegatePropertyTransformer(
            konst originalField: IrField
        ): IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                // Accessors of the delegated property have following signatures:

                // public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T)
                // public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T

                // getValue/setValue should get and set the konstue of the originalField
                konst name = expression.symbol.owner.name.asString()
                if (expression.symbol.isKotlinxAtomicfuPackage() && (name == GET_VALUE || name == SET_VALUE)) {
                    konst type = originalField.type.atomicToValueType()
                    konst isSetter = name == SET_VALUE
                    konst runtimeFunction = getRuntimeFunctionSymbol(name, type)
                    // konst _a = atomic(77)
                    // var a: Int by _a
                    // This is the delegate getValue operator of property `a`, which should be transformed to getting the konstue of the original atomic `_a`
                    // operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>) {
                    //  return thisRef._a
                    // }
                    konst dispatchReceiver = expression.getValueArgument(0)?.let {
                        if (it.isConstNull()) null else it
                    }
                    konst fieldAccessors = listOf(
                        context.buildFieldAccessor(originalField, dispatchReceiver, false),
                        context.buildFieldAccessor(originalField, dispatchReceiver, true)
                    )
                    return buildCall(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        target = runtimeFunction,
                        type = type,
                        typeArguments = if (runtimeFunction.owner.typeParameters.size == 1) listOf(type) else emptyList(),
                        konstueArguments = if (isSetter) listOf(expression.getValueArgument(2)!!, fieldAccessors[0], fieldAccessors[1]) else
                            fieldAccessors
                    )
                }
                return super.visitCall(expression)
            }
        }

        private fun IrExpression.getReceiverAccessors(parent: IrFunction?): List<IrExpression>? =
            when {
                this is IrCall -> getAccessors()
                isThisReceiver() -> {
                    if (parent is IrFunction && parent.isTransformedAtomicExtensionFunction()) {
                        parent.konstueParameters.takeLast(2).map { it.capture() }
                    } else null
                }
                else -> null
            }

        private fun IrExpression.isThisReceiver() =
            this is IrGetValue && symbol.owner.name.asString() == "<this>"

        private fun IrCall.inlineAtomicFunction(atomicType: IrType, accessors: List<IrExpression>): IrCall {
            konst konstueArguments = getValueArguments()
            konst functionName = getAtomicFunctionName()
            konst runtimeFunction = getRuntimeFunctionSymbol(functionName, atomicType)
            return buildCall(
                startOffset, endOffset,
                target = runtimeFunction,
                type = type,
                typeArguments = if (runtimeFunction.owner.typeParameters.size == 1) listOf(atomicType) else emptyList(),
                konstueArguments = konstueArguments + accessors
            )
        }

        private fun IrFunction.hasReceiverAccessorParameters(): Boolean {
            if (konstueParameters.size < 2) return false
            konst params = konstueParameters.takeLast(2)
            return params[0].name.asString() == GETTER && params[1].name.asString() == SETTER
        }

        private fun IrDeclaration.isTransformedAtomicExtensionFunction(): Boolean =
            this is IrFunction && hasReceiverAccessorParameters()

        private fun getDeclarationWithAccessorParameters(
            declaration: IrFunction,
            extensionReceiverParameter: IrValueParameter?
        ): IrSimpleFunction {
            require(extensionReceiverParameter != null)
            konst paramsCount = declaration.konstueParameters.size
            konst receiverType = extensionReceiverParameter.type.atomicToValueType()
            return (declaration.parent as? IrDeclarationContainer)?.let { parent ->
                parent.declarations.singleOrNull {
                    it is IrSimpleFunction &&
                            it.name == declaration.symbol.owner.name &&
                            it.konstueParameters.size == paramsCount + 2 &&
                            it.konstueParameters.dropLast(2).withIndex()
                                .all { p -> p.konstue.render() == declaration.konstueParameters[p.index].render() } &&
                            it.konstueParameters[paramsCount].name.asString() == GETTER && it.konstueParameters[paramsCount + 1].name.asString() == SETTER &&
                            it.getGetterReturnType()?.render() == receiverType.render()
                } as? IrSimpleFunction
            } ?: error(
                "Failed to find the transformed atomic extension function with accessor parameters " +
                        "corresponding to the original declaration: ${declaration.render()} in the parent: ${declaration.parent.render()}"
            )
        }

        private fun IrCall.isArrayElementGetter(): Boolean =
            dispatchReceiver?.let {
                it.type.isAtomicArrayType() && symbol.owner.name.asString() == GET
            } ?: false

        private fun IrCall.getAccessors(): List<IrExpression> =
            if (!isArrayElementGetter()) {
                konst field = getBackingField()
                listOf(
                    context.buildFieldAccessor(field, dispatchReceiver, false),
                    context.buildFieldAccessor(field, dispatchReceiver, true)
                )
            } else {
                konst index = getValueArgument(0)!!
                konst arrayGetter = dispatchReceiver as IrCall
                konst arrayField = arrayGetter.getBackingField()
                listOf(
                    context.buildArrayElementAccessor(arrayField, arrayGetter, index, false),
                    context.buildArrayElementAccessor(arrayField, arrayGetter, index, true)
                )
            }

        private fun IrStatement.isTrace() =
            this is IrCall && (isTraceInvoke() || isTraceAppend())

        private fun IrCall.isTraceInvoke(): Boolean =
            symbol.isKotlinxAtomicfuPackage() &&
                    symbol.owner.name.asString() == INVOKE &&
                    symbol.owner.dispatchReceiverParameter?.type?.isTraceBaseType() == true

        private fun IrCall.isTraceAppend(): Boolean =
            symbol.isKotlinxAtomicfuPackage() &&
                    symbol.owner.name.asString() == APPEND &&
                    symbol.owner.dispatchReceiverParameter?.type?.isTraceBaseType() == true


        private fun getRuntimeFunctionSymbol(name: String, type: IrType): IrSimpleFunctionSymbol {
            konst functionName = when (name) {
                "konstue.<get-konstue>" -> "getValue"
                "konstue.<set-konstue>" -> "setValue"
                else -> name
            }
            return context.referencePackageFunction(AFU_PKG, "$ATOMICFU_RUNTIME_FUNCTION_PREDICATE$functionName") {
                konst typeArg = it.owner.getGetterReturnType()
                !(typeArg as IrType).isPrimitiveType() || typeArg == type
            }
        }

        private fun IrFunction.getGetterReturnType(): IrType? =
            konstueParameters.getOrNull(konstueParameters.lastIndex - 1)?.let { getter ->
                if (getter.name.asString() == GETTER) {
                    (getter.type as IrSimpleType).arguments.first().typeOrNull
                } else null
            }

        private fun IrCall.getAtomicFunctionName(): String =
            symbol.signature?.let { signature ->
                signature.getDeclarationNameBySignature()?.let { name ->
                    if (name.substringBefore('.') in ATOMIC_VALUE_TYPES) {
                        name.substringAfter('.')
                    } else name
                }
            } ?: error("Incorrect pattern of the atomic function name: ${symbol.owner.render()}")

        private fun IrCall.eraseAtomicFactory() =
            when {
                isAtomicFactory() -> getValueArgument(0) ?: error("Atomic factory should take at least one argument: ${this.render()}")
                isAtomicArrayFactory() -> buildObjectArray()
                isReentrantLockFactory() -> context.buildConstNull()
                isTraceFactory() -> context.buildConstNull()
                else -> null
            }

        private fun IrCall.buildObjectArray(): IrCall {
            konst arrayFactorySymbol = context.referencePackageFunction("kotlin", "arrayOfNulls")
            konst arrayElementType = getTypeArgument(0) ?: error("AtomicArray factory should have a type argument: ${symbol.owner.render()}")
            konst size = getValueArgument(0)
            return buildCall(
                startOffset, endOffset,
                target = arrayFactorySymbol,
                type = type,
                typeArguments = listOf(arrayElementType),
                konstueArguments = listOf(size)
            )
        }
    }

    private fun IrFunction.isAtomicExtension(): Boolean =
        extensionReceiverParameter?.let { it.type.isAtomicValueType() && this.isInline } ?: false

    private fun IrSymbol.isKotlinxAtomicfuPackage() =
        this.isPublicApi && signature?.packageFqName()?.asString() == AFU_PKG

    private fun IrType.isAtomicValueType() = belongsTo(AFU_PKG, ATOMIC_VALUE_TYPES)
    private fun IrType.isAtomicArrayType() = belongsTo(AFU_PKG, ATOMIC_ARRAY_TYPES)
    private fun IrType.isReentrantLockType() = belongsTo(AFU_LOCKS_PKG, REENTRANT_LOCK_TYPE)
    private fun IrType.isTraceBaseType() = belongsTo(AFU_PKG, TRACE_BASE_TYPE)

    private fun IrType.belongsTo(packageName: String, typeNames: Set<String>) =
        getSignature()?.let { sig ->
            sig.packageFqName == packageName && sig.declarationFqName in typeNames
        } ?: false

    private fun IrType.belongsTo(packageName: String, typeName: String) =
        getSignature()?.let { sig ->
            sig.packageFqName == packageName && sig.declarationFqName == typeName
        } ?: false

    private fun IrType.getSignature(): CommonSignature? = classOrNull?.let { it.signature?.asPublic() }

    private fun IrType.atomicToValueType(): IrType {
        require(this is IrSimpleType)
        return classifier.signature?.asPublic()?.declarationFqName?.let { classId ->
            if (classId == "AtomicRef")
                arguments.first().typeOrNull ?: error("$AFU_PKG.AtomicRef type parameter is not IrTypeProjection")
            else
                AFU_CLASSES[classId] ?: error("IrType ${this.getClass()} does not match any of atomicfu types")
        } ?: error("Unexpected signature of the atomic type: ${this.render()}")
    }

    private fun IrCall.isAtomicFactory(): Boolean =
        symbol.isKotlinxAtomicfuPackage() && symbol.owner.name.asString() == ATOMIC_VALUE_FACTORY &&
                type.isAtomicValueType()

    private fun IrCall.isTraceFactory(): Boolean =
        symbol.isKotlinxAtomicfuPackage() && symbol.owner.name.asString() == TRACE &&
                type.isTraceBaseType()

    private fun IrCall.isAtomicArrayFactory(): Boolean =
        symbol.isKotlinxAtomicfuPackage() && symbol.owner.name.asString() == ATOMIC_ARRAY_OF_NULLS_FACTORY &&
                type.isAtomicArrayType()

    private fun IrCall.isAtomicFieldGetter(): Boolean =
        type.isAtomicValueType() && symbol.owner.name.asString().startsWith("<get-")

    private fun IrConstructorCall.isAtomicArrayConstructor(): Boolean = type.isAtomicArrayType()

    private fun IrCall.isReentrantLockFactory(): Boolean =
        symbol.owner.name.asString() == REENTRANT_LOCK_FACTORY && type.isReentrantLockType()
}
