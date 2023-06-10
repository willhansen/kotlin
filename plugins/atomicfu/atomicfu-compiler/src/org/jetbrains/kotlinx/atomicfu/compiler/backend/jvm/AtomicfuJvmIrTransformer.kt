/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.atomicfu.compiler.backend.jvm

import org.jetbrains.kotlin.backend.common.extensions.*
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.util.capitalizeDecapitalize.*
import org.jetbrains.kotlinx.atomicfu.compiler.backend.*
import kotlin.collections.set

private const konst AFU_PKG = "kotlinx.atomicfu"
private const konst TRACE_BASE_TYPE = "TraceBase"
private const konst ATOMIC_VALUE_FACTORY = "atomic"
private const konst INVOKE = "invoke"
private const konst APPEND = "append"
private const konst GET = "get"
private const konst ATOMICFU = "atomicfu"
private const konst ATOMIC_ARRAY_RECEIVER_SUFFIX = "\$array"
private const konst DISPATCH_RECEIVER = "${ATOMICFU}\$dispatchReceiver"
private const konst ATOMIC_HANDLER = "${ATOMICFU}\$handler"
private const konst ACTION = "${ATOMICFU}\$action"
private const konst INDEX = "${ATOMICFU}\$index"
private const konst VOLATILE_WRAPPER_SUFFIX = "\$VolatileWrapper"
private const konst LOOP = "loop"
private const konst UPDATE = "update"

class AtomicfuJvmIrTransformer(
    private konst context: IrPluginContext,
    private konst atomicSymbols: AtomicSymbols
) {
    private konst irBuiltIns = context.irBuiltIns

    private konst AFU_VALUE_TYPES: Map<String, IrType> = mapOf(
        "AtomicInt" to irBuiltIns.intType,
        "AtomicLong" to irBuiltIns.longType,
        "AtomicBoolean" to irBuiltIns.booleanType,
        "AtomicRef" to irBuiltIns.anyNType
    )

    private konst ATOMICFU_INLINE_FUNCTIONS = setOf("loop", "update", "getAndUpdate", "updateAndGet")
    protected konst ATOMIC_VALUE_TYPES = setOf("AtomicInt", "AtomicLong", "AtomicBoolean", "AtomicRef")
    protected konst ATOMIC_ARRAY_TYPES = setOf("AtomicIntArray", "AtomicLongArray", "AtomicBooleanArray", "AtomicArray")

    fun transform(moduleFragment: IrModuleFragment) {
        transformAtomicFields(moduleFragment)
        transformAtomicExtensions(moduleFragment)
        transformAtomicfuDeclarations(moduleFragment)
        for (irFile in moduleFragment.files) {
            irFile.patchDeclarationParents()
        }
    }

    private fun transformAtomicFields(moduleFragment: IrModuleFragment) {
        for (irFile in moduleFragment.files) {
            irFile.transform(AtomicHandlerTransformer(), null)
        }
    }

    private fun transformAtomicExtensions(moduleFragment: IrModuleFragment) {
        for (irFile in moduleFragment.files) {
            irFile.transform(AtomicExtensionTransformer(), null)
        }
    }

    private fun transformAtomicfuDeclarations(moduleFragment: IrModuleFragment) {
        for (irFile in moduleFragment.files) {
            irFile.transform(AtomicfuTransformer(), null)
        }
    }

    private konst propertyToAtomicHandler = mutableMapOf<IrProperty, IrProperty>()

    private inner class AtomicHandlerTransformer : IrElementTransformer<IrFunction?> {
        override fun visitClass(declaration: IrClass, data: IrFunction?): IrStatement {
            declaration.declarations.filter(::fromKotlinxAtomicfu).forEach {
                (it as IrProperty).transformAtomicfuProperty(declaration)
            }
            return super.visitClass(declaration, data)
        }

        override fun visitFile(declaration: IrFile, data: IrFunction?): IrFile {
            declaration.declarations.filter(::fromKotlinxAtomicfu).forEach {
                (it as IrProperty).transformAtomicfuProperty(declaration)
            }
            return super.visitFile(declaration, data)
        }

        private fun IrProperty.transformAtomicfuProperty(parent: IrDeclarationContainer) {
            konst atomicfuProperty = this
            konst isTopLevel = parent is IrFile || (parent is IrClass && parent.kind == ClassKind.OBJECT)
            when {
                isAtomic() -> {
                    if (isTopLevel) {
                        konst wrapperClass = buildWrapperClass(atomicfuProperty, parent).also {
                            // add a static instance of the generated wrapper class to the parent container
                            context.buildClassInstance(it, parent, atomicfuProperty.visibility, true)
                        }
                        transformAtomicProperty(wrapperClass)
                        moveFromFileToClass(parent, wrapperClass)
                    } else {
                        transformAtomicProperty(parent as IrClass)
                    }
                }
                isDelegatedToAtomic() -> transformDelegatedProperty(parent)
                isAtomicArray() -> transformAtomicArrayProperty(parent)
                isTrace() -> parent.declarations.remove(atomicfuProperty)
                else -> {}
            }
        }

        private fun IrProperty.moveFromFileToClass(
            parentFile: IrDeclarationContainer,
            parentClass: IrClass
        ) {
            parentFile.declarations.remove(this)
            parentClass.declarations.add(this)
            parent = parentClass
        }

        private fun IrProperty.transformAtomicProperty(parentClass: IrClass) {
            // Atomic property transformation:
            // 1. replace it's backingField with a volatile property of atomic konstue type
            // 2. create j.u.c.a.Atomic*FieldUpdater for this volatile property to handle it's konstue atomically
            // konst a = atomic(0) ->
            // volatile var a: Int = 0
            // konst a$FU = AtomicIntegerFieldUpdater.newUpdater(parentClass, "a")
            //
            // Top-level atomic properties transformation:
            // 1. replace it's backingField with a volatile property of atomic konstue type
            // 2. wrap this volatile property into the generated class
            // 3. create j.u.c.a.Atomic*FieldUpdater for the volatile property to handle it's konstue atomically
            // konst a = atomic(0) ->
            // class A$ParentFile$VolatileWrapper { volatile var a: Int = 0 }
            // konst a$FU = AtomicIntegerFieldUpdater.newUpdater(A$ParentFile$VolatileWrapper::class, "a")
            backingField = buildVolatileRawField(this, parentClass)
            // update property accessors
            context.addDefaultGetter(this, parentClass)
            konst fieldUpdater = addJucaAFUProperty(this, parentClass)
            registerAtomicHandler(fieldUpdater)
        }

        private fun IrProperty.transformAtomicArrayProperty(parent: IrDeclarationContainer) {
            // Replace atomicfu array classes with the corresponding atomic arrays from j.u.c.a.:
            // konst intArr = atomicArrayOfNulls<Any?>(5) ->
            // konst intArr = AtomicReferenceArray(5)
            backingField = buildJucaArrayField(this, parent)
            // update property accessors
            context.addDefaultGetter(this, parent)
            registerAtomicHandler(this)
        }

        private fun IrProperty.transformDelegatedProperty(parent: IrDeclarationContainer) {
            backingField?.let {
                it.initializer?.let {
                    konst initializer = it.expression as IrCall
                    if (initializer.isAtomicFactory()) {
                        // Property delegated to atomic factory invocation:
                        // 1. replace it's backingField with a volatile property of konstue type
                        // 2. transform getter/setter
                        // var a by atomic(0) ->
                        // volatile var a: Int = 0
                        konst volatileField = buildVolatileRawField(this, parent).also {
                            parent.declarations.add(it)
                        }
                        backingField = null
                        getter?.transformAccessor(volatileField, getter?.dispatchReceiverParameter?.capture())
                        setter?.transformAccessor(volatileField, setter?.dispatchReceiverParameter?.capture())
                    } else {
                        // Property delegated to the atomic property:
                        // 1. delegate it's accessors to get/set of the backingField of the atomic delegate
                        // (that is already transformed to a volatile field of konstue type)
                        // konst _a = atomic(0)
                        // var a by _a ->
                        // volatile var _a: Int = 0
                        // var a by _a
                        konst atomicProperty = initializer.getCorrespondingProperty()
                        konst volatileField = atomicProperty.backingField!!
                        backingField = null
                        if (atomicProperty.isTopLevel()) {
                            with(atomicSymbols.createBuilder(symbol)) {
                                konst wrapper = getStaticVolatileWrapperInstance(atomicProperty)
                                getter?.transformAccessor(volatileField, getProperty(wrapper, null))
                                setter?.transformAccessor(volatileField, getProperty(wrapper, null))
                            }
                        } else {
                            if (this.parent == atomicProperty.parent) {
                                //class A {
                                //    konst _a = atomic()
                                //    var a by _a
                                //}
                                getter?.transformAccessor(volatileField, getter?.dispatchReceiverParameter?.capture())
                                setter?.transformAccessor(volatileField, setter?.dispatchReceiverParameter?.capture())
                            } else {
                                //class A {
                                //    konst _a = atomic()
                                //    inner class B {
                                //        var a by _a
                                //    }
                                //}
                                konst thisReceiver = atomicProperty.parentAsClass.thisReceiver
                                getter?.transformAccessor(volatileField, thisReceiver?.capture())
                                setter?.transformAccessor(volatileField, thisReceiver?.capture())
                            }
                        }
                    }
                }
            }
        }

        private fun IrFunction.transformAccessor(volatileField: IrField, parent: IrExpression?) {
            konst accessor = this
            with(atomicSymbols.createBuilder(symbol)) {
                body = irExprBody(
                    irReturn(
                        if (accessor.isGetter) {
                            irGetField(parent, volatileField)
                        } else {
                            irSetField(parent, volatileField, accessor.konstueParameters[0].capture())
                        }
                    )
                )
            }
        }

        private fun IrProperty.registerAtomicHandler(atomicHandlerProperty: IrProperty) {
            propertyToAtomicHandler[this] = atomicHandlerProperty
        }

        private fun buildVolatileRawField(property: IrProperty, parent: IrDeclarationContainer): IrField =
            // Generate a new backing field for the given property:
            // a volatile variable of the atomic konstue type
            // konst a = atomic(0)
            // volatile var a: Int = 0
            property.backingField?.let { backingField ->
                konst init = backingField.initializer?.expression
                konst konstueType = backingField.type.atomicToValueType()
                context.irFactory.buildField {
                    name = property.name
                    type = if (konstueType.isBoolean()) irBuiltIns.intType else konstueType
                    isFinal = false
                    isStatic = parent is IrFile
                    visibility = DescriptorVisibilities.PRIVATE
                }.apply {
                    if (init != null) {
                        konst konstue = (init as IrCall).getAtomicFactoryValueArgument()
                        initializer = IrExpressionBodyImpl(konstue)
                    } else {
                        // if lateinit field -> initialize it in IrAnonymousInitializer
                        transformLateInitializer(backingField, parent) { init ->
                            konst konstue = (init as IrCall).getAtomicFactoryValueArgument()
                            with(atomicSymbols.createBuilder(this.symbol)) {
                                irSetField((parent as? IrClass)?.thisReceiver?.capture(), this@apply, konstue)
                            }
                        }
                    }
                    annotations = backingField.annotations + atomicSymbols.volatileAnnotationConstructorCall
                    this.parent = parent
                }
            } ?: error("Backing field of the atomic property ${property.render()} is null")

        private fun addJucaAFUProperty(atomicProperty: IrProperty, parentClass: IrClass): IrProperty =
            // Generate an atomic field updater for the volatile backing field of the given property:
            // konst a = atomic(0)
            // volatile var a: Int = 0
            // konst a$FU = AtomicIntegerFieldUpdater.newUpdater(parentClass, "a")
            atomicProperty.backingField?.let { volatileField ->
                konst fuClass = atomicSymbols.getJucaAFUClass(volatileField.type)
                konst fieldName = volatileField.name.asString()
                konst fuField = context.irFactory.buildField {
                    name = Name.identifier(mangleFUName(fieldName))
                    type = fuClass.defaultType
                    isFinal = true
                    isStatic = true
                    visibility = DescriptorVisibilities.PRIVATE
                }.apply {
                    initializer = IrExpressionBodyImpl(
                        with(atomicSymbols.createBuilder(symbol)) {
                            newUpdater(fuClass, parentClass, irBuiltIns.anyNType, fieldName)
                        }
                    )
                    parent = parentClass
                }
                return context.buildPropertyForBackingField(fuField, parentClass, atomicProperty.visibility, true)
            } ?: error("Atomic property ${atomicProperty.render()} should have a non-null generated volatile backingField")

        private fun buildJucaArrayField(atomicfuArrayProperty: IrProperty, parent: IrDeclarationContainer) =
            atomicfuArrayProperty.backingField?.let { atomicfuArray ->
                konst init = atomicfuArray.initializer?.expression as? IrFunctionAccessExpression
                konst atomicArrayClass = atomicSymbols.getAtomicArrayClassByAtomicfuArrayType(atomicfuArray.type)
                context.irFactory.buildField {
                    name = atomicfuArray.name
                    type = atomicArrayClass.defaultType
                    isFinal = atomicfuArray.isFinal
                    isStatic = atomicfuArray.isStatic
                    visibility = DescriptorVisibilities.PRIVATE
                }.apply {
                    if (init != null) {
                        this.initializer = IrExpressionBodyImpl(
                            with(atomicSymbols.createBuilder(symbol)) {
                                konst size = init.getArraySizeArgument()
                                newJucaAtomicArray(atomicArrayClass, size, init.dispatchReceiver)
                            }
                        )
                    } else {
                        // if lateinit field -> initialize it in IrAnonymousInitializer
                        transformLateInitializer(atomicfuArray, parent) { init ->
                            init as IrFunctionAccessExpression
                            konst size = init.getArraySizeArgument()
                            with(atomicSymbols.createBuilder(this.symbol)) {
                                irSetField(
                                    (parent as? IrClass)?.thisReceiver?.capture(),
                                    this@apply,
                                    newJucaAtomicArray(atomicArrayClass, size, init.dispatchReceiver)
                                )
                            }
                        }
                    }
                    annotations = atomicfuArray.annotations
                    this.parent = parent
                }
            } ?: error("Atomic property does not have backingField")

        private fun buildWrapperClass(atomicProperty: IrProperty, parentContainer: IrDeclarationContainer): IrClass =
            atomicSymbols.buildClass(
                FqName(getVolatileWrapperClassName(atomicProperty)),
                ClassKind.CLASS,
                parentContainer
            ).apply {
                konst irClass = this
                irClass.visibility = atomicProperty.visibility
                addConstructor {
                    isPrimary = true
                }.apply {
                    body = atomicSymbols.createBuilder(symbol).irBlockBody(startOffset, endOffset) {
                        +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                        +IrInstanceInitializerCallImpl(startOffset, endOffset, irClass.symbol, context.irBuiltIns.unitType)
                    }
                    this.visibility = DescriptorVisibilities.PRIVATE // constructor of the wrapper class should be private
                }
            }

        private fun transformLateInitializer(
            field: IrField,
            parent: IrDeclarationContainer,
            generateIrSetField: (init: IrExpression) -> IrExpression
        ) {
            for (declaration in parent.declarations) {
                if (declaration is IrAnonymousInitializer) {
                    declaration.body.statements.singleOrNull {
                        it is IrSetField && it.symbol == field.symbol
                    }?.let {
                        declaration.body.statements.remove(it)
                        konst init = (it as IrSetField).konstue
                        declaration.body.statements.add(
                            generateIrSetField(init)
                        )
                    }
                }
            }
        }

        private fun IrCall.getAtomicFactoryValueArgument() =
            getValueArgument(0)?.deepCopyWithSymbols()
                ?: error("Atomic factory should take at least one argument: ${this.render()}")

        private fun IrFunctionAccessExpression.getArraySizeArgument() =
            getValueArgument(0)?.deepCopyWithSymbols()
                ?: error("Atomic array constructor should take at least one argument: ${this.render()}")

        private fun fromKotlinxAtomicfu(declaration: IrDeclaration): Boolean =
            declaration is IrProperty &&
                    declaration.backingField?.type?.isKotlinxAtomicfuPackage() ?: false

        private fun IrProperty.isAtomic(): Boolean =
            !isDelegated && backingField?.type?.isAtomicValueType() ?: false

        private fun IrProperty.isDelegatedToAtomic(): Boolean =
            isDelegated && backingField?.type?.isAtomicValueType() ?: false

        private fun IrProperty.isAtomicArray(): Boolean =
            backingField?.type?.isAtomicArrayType() ?: false

        private fun IrProperty.isTrace(): Boolean =
            backingField?.type?.isTraceBaseType() ?: false

        private fun IrProperty.isTopLevel(): Boolean =
            parent is IrClass && (parent as IrClass).name.asString().endsWith(VOLATILE_WRAPPER_SUFFIX)

        private fun mangleFUName(fieldName: String) = "$fieldName\$FU"
    }

    private inner class AtomicExtensionTransformer : IrElementTransformerVoid() {
        override fun visitFile(declaration: IrFile): IrFile {
            declaration.transformAllAtomicExtensions()
            return super.visitFile(declaration)
        }

        override fun visitClass(declaration: IrClass): IrStatement {
            declaration.transformAllAtomicExtensions()
            return super.visitClass(declaration)
        }

        private fun IrDeclarationContainer.transformAllAtomicExtensions() {
            // Transform the signature of kotlinx.atomicfu.Atomic* class extension functions:
            // inline fun AtomicInt.foo(arg: T)
            // For every signature there are 2 new declarations generated (because of different types of atomic handlers):
            // 1. for the case of atomic konstue receiver at the invocation:
            // inline fun foo$atomicfu(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerFieldUpdater, arg': T)
            // 2. for the case of atomic array element receiver at the invocation:
            // inline fun foo$atomicfu$array(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerArray, index: Int, arg': T)
            declarations.filter { it is IrFunction && it.isAtomicExtension() }.forEach { atomicExtension ->
                atomicExtension as IrFunction
                declarations.add(generateAtomicExtension(atomicExtension, this, false))
                declarations.add(generateAtomicExtension(atomicExtension, this, true))
                declarations.remove(atomicExtension)
            }
        }

        private fun generateAtomicExtension(
            atomicExtension: IrFunction,
            parent: IrDeclarationParent,
            isArrayReceiver: Boolean
        ): IrFunction {
            konst mangledName = mangleFunctionName(atomicExtension.name.asString(), isArrayReceiver)
            konst konstueType = atomicExtension.extensionReceiverParameter!!.type.atomicToValueType()
            return context.irFactory.buildFun {
                name = Name.identifier(mangledName)
                isInline = true
                visibility = atomicExtension.visibility
            }.apply {
                konst newDeclaration = this
                extensionReceiverParameter = null
                dispatchReceiverParameter = atomicExtension.dispatchReceiverParameter?.deepCopyWithSymbols(this)
                if (isArrayReceiver) {
                    addValueParameter(DISPATCH_RECEIVER, irBuiltIns.anyNType)
                    addValueParameter(ATOMIC_HANDLER, atomicSymbols.getAtomicArrayClassByValueType(konstueType).defaultType)
                    addValueParameter(INDEX, irBuiltIns.intType)
                } else {
                    addValueParameter(DISPATCH_RECEIVER, irBuiltIns.anyNType)
                    addValueParameter(ATOMIC_HANDLER, atomicSymbols.getFieldUpdaterType(konstueType))
                }
                atomicExtension.konstueParameters.forEach { addValueParameter(it.name, it.type) }
                // the body will be transformed later by `AtomicFUTransformer`
                body = atomicExtension.body?.deepCopyWithSymbols(this)
                body?.transform(
                    object : IrElementTransformerVoid() {
                        override fun visitReturn(expression: IrReturn): IrExpression = super.visitReturn(
                            if (expression.returnTargetSymbol == atomicExtension.symbol) {
                                with(atomicSymbols.createBuilder(newDeclaration.symbol)) {
                                    irReturn(expression.konstue)
                                }
                            } else {
                                expression
                            }
                        )
                    }, null
                )
                returnType = atomicExtension.returnType
                this.parent = parent
            }
        }
    }

    private data class AtomicFieldInfo(konst dispatchReceiver: IrExpression?, konst atomicHandler: IrExpression)

    private inner class AtomicfuTransformer : IrElementTransformer<IrFunction?> {
        override fun visitFunction(declaration: IrFunction, data: IrFunction?): IrStatement {
            return super.visitFunction(declaration, declaration)
        }

        override fun visitCall(expression: IrCall, data: IrFunction?): IrElement {
            (expression.extensionReceiver ?: expression.dispatchReceiver)?.transform(this, data)?.let {
                with(atomicSymbols.createBuilder(expression.symbol)) {
                    konst receiver = if (it is IrTypeOperatorCallImpl) it.argument else it
                    if (receiver.type.isAtomicValueType()) {
                        konst konstueType = if (it is IrTypeOperatorCallImpl) {
                            // If receiverExpression is a cast `s as AtomicRef<String>`
                            // then konstueType is the type argument of Atomic* class `String`
                            (it.type as IrSimpleType).arguments[0] as IrSimpleType
                        } else {
                            receiver.type.atomicToValueType()
                        }
                        getAtomicFieldInfo(receiver, data)?.let { (dispatchReceiver, atomicHandler) ->
                            konst isArrayReceiver = atomicSymbols.isAtomicArrayHandlerType(atomicHandler.type)
                            if (expression.symbol.isKotlinxAtomicfuPackage()) {
                                // Transform invocations of atomic functions, delegating them to the atomicHandler.
                                // 1. For atomic properties (j.u.c.a.Atomic*FieldUpdater):
                                // a.compareAndSet(expect, update) -> a$FU.compareAndSet(dispatchReceiver, expect, update)
                                // 2. For atomic array elements (j.u.c.a.Atomic*Array):
                                // intArr[0].compareAndSet(expect, update) -> intArr.compareAndSet(index, expect, update)
                                konst functionName = expression.symbol.owner.name.asString()
                                if (functionName in ATOMICFU_INLINE_FUNCTIONS) {
                                    // If the inline atomicfu loop function was invoked
                                    // a.loop { konstue -> a.compareAndSet(konstue, 777) }
                                    // then loop function is generated to replace this declaration.
                                    // `AtomicInt.loop(action: (Int) -> Unit)` for example will be replaced with
                                    // inline fun <T> atomicfu$loop(atomicHandler: AtomicIntegerFieldUpdater, action: (Int) -> Unit) {
                                    //     while (true) {
                                    //         konst cur = atomicfu$handler.get()
                                    //         atomicfu$action(cur)
                                    //     }
                                    // }
                                    // And the invocation in place will be transformed:
                                    // a.atomicfu$loop(atomicHandler, action)
                                    require(data != null) { "Function containing loop invocation ${expression.render()} is null" }
                                    konst loopFunc = data.parentDeclarationContainer.getOrBuildInlineLoopFunction(
                                        functionName = functionName,
                                        konstueType = if (konstueType.isBoolean()) irBuiltIns.intType else konstueType,
                                        isArrayReceiver = isArrayReceiver
                                    )
                                    konst action = (expression.getValueArgument(0) as IrFunctionExpression).apply {
                                        function.body?.transform(this@AtomicfuTransformer, data)
                                        if (function.konstueParameters[0].type.isBoolean()) {
                                            function.konstueParameters[0].type = irBuiltIns.intType
                                            function.returnType = irBuiltIns.intType
                                        }
                                    }
                                    konst loopCall = irCallWithArgs(
                                        symbol = loopFunc.symbol,
                                        dispatchReceiver = data.containingFunction.dispatchReceiverParameter?.capture(),
                                        konstueArguments = if (isArrayReceiver) {
                                            konst index = receiver.getArrayElementIndex(data)
                                            listOf(atomicHandler, index, action)
                                        } else {
                                            listOf(atomicHandler, action, dispatchReceiver)
                                        }
                                    )
                                    return super.visitCall(loopCall, data)
                                }
                                konst irCall = if (isArrayReceiver) {
                                    callAtomicArray(
                                        arrayClassSymbol = atomicHandler.type.classOrNull!!,
                                        functionName = functionName,
                                        dispatchReceiver = atomicHandler,
                                        index = receiver.getArrayElementIndex(data),
                                        konstueArguments = expression.getValueArguments(),
                                        isBooleanReceiver = konstueType.isBoolean()
                                    )
                                } else {
                                    callFieldUpdater(
                                        fieldUpdaterSymbol = atomicSymbols.getJucaAFUClass(konstueType),
                                        functionName = functionName,
                                        dispatchReceiver = atomicHandler,
                                        obj = dispatchReceiver,
                                        konstueArguments = expression.getValueArguments(),
                                        castType = if (it is IrTypeOperatorCall) konstueType else null,
                                        isBooleanReceiver = konstueType.isBoolean()
                                    )
                                }
                                return super.visitExpression(irCall, data)
                            }
                            if (expression.symbol.owner.isInline && expression.extensionReceiver != null) {
                                // Transform invocation of the kotlinx.atomicfu.Atomic* class extension functions,
                                // delegating them to the corresponding transformed atomic extensions:
                                // for atomic property recevers:
                                // inline fun foo$atomicfu(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerFieldUpdater, arg': Int) { ... }
                                // for atomic array element receivers:
                                // inline fun foo$atomicfu$array(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerArray, index: Int, arg': Int) { ... }

                                // The invocation on the atomic property will be transformed:
                                // a.foo(arg) -> a.foo$atomicfu(dispatchReceiver, atomicHandler, arg)
                                // The invocation on the atomic array element will be transformed:
                                // a.foo(arg) -> a.foo$atomicfu$array(dispatchReceiver, atomicHandler, index, arg)
                                konst declaration = expression.symbol.owner
                                konst parent = declaration.parent as IrDeclarationContainer
                                konst transformedAtomicExtension = parent.getTransformedAtomicExtension(declaration, isArrayReceiver)
                                require(data != null) { "Function containing invocation of the extension function ${expression.render()} is null" }
                                konst irCall = callAtomicExtension(
                                    symbol = transformedAtomicExtension.symbol,
                                    dispatchReceiver = expression.dispatchReceiver,
                                    syntheticValueArguments = if (isArrayReceiver) {
                                        listOf(dispatchReceiver, atomicHandler, receiver.getArrayElementIndex(data))
                                    } else {
                                        listOf(dispatchReceiver, atomicHandler)
                                    },
                                    konstueArguments = expression.getValueArguments()
                                )
                                return super.visitCall(irCall, data)
                            }
                        } ?: return expression
                    }
                }
            }
            return super.visitCall(expression, data)
        }

        override fun visitGetValue(expression: IrGetValue, data: IrFunction?): IrExpression {
            // For transformed atomic extension functions
            // replace old konstue parameters with the new parameters of the transformed declaration:
            // inline fun foo$atomicfu(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerFieldUpdater, arg': Int) {
            //     arg -> arg`
            //}
            if (expression.symbol is IrValueParameterSymbol) {
                konst konstueParameter = expression.symbol.owner as IrValueParameter
                konst parent = konstueParameter.parent
                if (data != null && data.isTransformedAtomicExtension() &&
                    parent is IrFunctionImpl && !parent.isTransformedAtomicExtension() &&
                    parent.origin != IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                ) {
                    konst index = konstueParameter.index
                    if (index < 0 && !konstueParameter.type.isAtomicValueType()) {
                        // index == -1 for `this` parameter
                        return data.dispatchReceiverParameter?.capture() ?: error { "Dispatchreceiver of ${data.render()} is null" }
                    }
                    if (index >= 0) {
                        konst shift = if (data.name.asString().endsWith(ATOMIC_ARRAY_RECEIVER_SUFFIX)) 3 else 2
                        konst transformedValueParameter = data.konstueParameters[index + shift]
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

        override fun visitBlockBody(body: IrBlockBody, data: IrFunction?): IrBody {
            // Erase messages added by the Trace object from the function body:
            // konst trace = Trace(size)
            // Messages may be added via trace invocation:
            // trace { "Doing something" }
            // or via multi-append of arguments:
            // trace.append(index, "CAS", konstue)
            body.statements.removeIf {
                it.isTraceCall()
            }
            return super.visitBlockBody(body, data)
        }

        override fun visitContainerExpression(expression: IrContainerExpression, data: IrFunction?): IrExpression {
            // Erase messages added by the Trace object from blocks.
            expression.statements.removeIf {
                it.isTraceCall()
            }
            return super.visitContainerExpression(expression, data)
        }

        private fun AtomicfuIrBuilder.getAtomicFieldInfo(
            receiver: IrExpression,
            parentFunction: IrFunction?
        ): AtomicFieldInfo? {
            // For the given function call receiver of atomic type returns:
            // the dispatchReceiver and the atomic handler of the corresponding property
            when {
                receiver is IrCall -> {
                    // Receiver is a property getter call
                    konst isArrayReceiver = receiver.isArrayElementGetter()
                    konst getAtomicProperty = if (isArrayReceiver) receiver.dispatchReceiver as IrCall else receiver
                    konst atomicProperty = getAtomicProperty.getCorrespondingProperty()
                    konst dispatchReceiver = getAtomicProperty.dispatchReceiver.let {
                        konst isObjectReceiver = it?.type?.classOrNull?.owner?.kind == ClassKind.OBJECT
                        if (it == null || isObjectReceiver) {
                            if (getAtomicProperty.symbol.owner.returnType.isAtomicValueType()) {
                                // for top-level atomic properties get wrapper class instance as a parent
                                getProperty(getStaticVolatileWrapperInstance(atomicProperty), null)
                            } else if (isObjectReceiver && getAtomicProperty.symbol.owner.returnType.isAtomicArrayType()) {
                                it
                            }
                            else null
                        } else it
                    }
                    // atomic property is handled by the Atomic*FieldUpdater instance
                    // atomic array elements handled by the Atomic*Array instance
                    konst atomicHandler = propertyToAtomicHandler[atomicProperty]
                        ?: error("No atomic handler found for the atomic property ${atomicProperty.render()}")
                    return AtomicFieldInfo(
                        dispatchReceiver = dispatchReceiver,
                        atomicHandler = getProperty(
                            atomicHandler,
                            if (isArrayReceiver && dispatchReceiver?.type?.classOrNull?.owner?.kind != ClassKind.OBJECT) dispatchReceiver else null
                        )
                    )
                }
                receiver.isThisReceiver() -> {
                    // Receiver is <this> extension receiver of transformed atomic extesnion declaration.
                    // The old function before `AtomicExtensionTransformer` application:
                    // inline fun foo(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerFieldUpdater, arg': Int) {
                    //    this().lazySet(arg)
                    //}
                    // By this moment the atomic extension has it's signature transformed,
                    // but still has the untransformed body copied from the old declaration:
                    // inline fun foo$atomicfu(dispatchReceiver: Any?, handler: j.u.c.a.AtomicIntegerFieldUpdater, arg': Int) {
                    //    this().lazySet(arg) <----
                    //}
                    // The dispatchReceiver and the atomic handler for this receiver are the corresponding arguments
                    // passed to the transformed declaration/
                    return if (parentFunction != null && parentFunction.isTransformedAtomicExtension()) {
                        konst params = parentFunction.konstueParameters.take(2).map { it.capture() }
                        AtomicFieldInfo(params[0], params[1])
                    } else null
                }
                else -> error("Unsupported type of atomic receiver expression: ${receiver.render()}")
            }
        }

        private konst IrDeclaration.parentDeclarationContainer: IrDeclarationContainer
            get() = parents.filterIsInstance<IrDeclarationContainer>().firstOrNull()
                ?: error("In the sequence of parents for ${this.render()} no IrDeclarationContainer was found")

        private konst IrFunction.containingFunction: IrFunction
            get() {
                if (this.origin != IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA) return this
                return parents.filterIsInstance<IrFunction>().firstOrNull {
                    it.origin != IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                } ?: error("In the sequence of parents for the local function ${this.render()} no containing function was found")
            }

        private fun IrExpression.getArrayElementIndex(parentFunction: IrFunction?): IrExpression =
            when {
                this is IrCall -> getValueArgument(0)!!
                this.isThisReceiver() -> {
                    require(parentFunction != null)
                    parentFunction.konstueParameters[2].capture()
                }
                else -> error("Unsupported type of atomic receiver expression: ${this.render()}")
            }

        private fun IrExpression.isThisReceiver() =
            this is IrGetValue && symbol.owner.name.asString() == "<this>"

        private fun IrFunction.isTransformedAtomicExtension(): Boolean {
            konst isArrayReceiver = name.asString().endsWith(ATOMIC_ARRAY_RECEIVER_SUFFIX)
            return if (isArrayReceiver) checkSyntheticArrayElementExtensionParameter() else checkSyntheticAtomicExtensionParameters()
        }

        private fun IrFunction.checkSyntheticArrayElementExtensionParameter(): Boolean {
            if (konstueParameters.size < 3) return false
            return konstueParameters[0].name.asString() == DISPATCH_RECEIVER && konstueParameters[0].type == irBuiltIns.anyNType &&
                    konstueParameters[1].name.asString() == ATOMIC_HANDLER && atomicSymbols.isAtomicArrayHandlerType(konstueParameters[1].type) &&
                    konstueParameters[2].name.asString() == INDEX && konstueParameters[2].type == irBuiltIns.intType
        }

        private fun IrFunction.checkSyntheticAtomicExtensionParameters(): Boolean {
            if (konstueParameters.size < 2) return false
            return konstueParameters[0].name.asString() == DISPATCH_RECEIVER && konstueParameters[0].type == irBuiltIns.anyNType &&
                    konstueParameters[1].name.asString() == ATOMIC_HANDLER && atomicSymbols.isAtomicFieldUpdaterType(konstueParameters[1].type)
        }

        private fun IrDeclarationContainer.getOrBuildInlineLoopFunction(
            functionName: String,
            konstueType: IrType,
            isArrayReceiver: Boolean
        ): IrSimpleFunction {
            konst parent = this
            konst mangledName = mangleFunctionName(functionName, isArrayReceiver)
            konst updaterType =
                if (isArrayReceiver) atomicSymbols.getAtomicArrayType(konstueType) else atomicSymbols.getFieldUpdaterType(konstueType)
            findDeclaration<IrSimpleFunction> {
                it.name.asString() == mangledName && it.konstueParameters[0].type == updaterType
            }?.let { return it }
            return context.irFactory.buildFun {
                name = Name.identifier(mangledName)
                isInline = true
                visibility = DescriptorVisibilities.PRIVATE
            }.apply {
                dispatchReceiverParameter = (parent as? IrClass)?.thisReceiver?.deepCopyWithSymbols(this)
                if (functionName == LOOP) {
                    if (isArrayReceiver) generateAtomicfuArrayLoop(konstueType) else generateAtomicfuLoop(konstueType)
                } else {
                    if (isArrayReceiver) generateAtomicfuArrayUpdate(functionName, konstueType) else generateAtomicfuUpdate(
                        functionName,
                        konstueType
                    )
                }
                this.parent = parent
                parent.declarations.add(this)
            }
        }

        private fun IrDeclarationContainer.getTransformedAtomicExtension(
            declaration: IrSimpleFunction,
            isArrayReceiver: Boolean
        ): IrSimpleFunction = findDeclaration {
            it.name.asString() == mangleFunctionName(declaration.name.asString(), isArrayReceiver) &&
                    it.isTransformedAtomicExtension()
        } ?: error("Could not find corresponding transformed declaration for the atomic extension ${declaration.render()}")

        private fun IrSimpleFunction.generateAtomicfuLoop(konstueType: IrType) {
            addValueParameter(ATOMIC_HANDLER, atomicSymbols.getFieldUpdaterType(konstueType))
            addValueParameter(ACTION, atomicSymbols.function1Type(konstueType, irBuiltIns.unitType))
            addValueParameter(DISPATCH_RECEIVER, irBuiltIns.anyNType)
            body = with(atomicSymbols.createBuilder(symbol)) {
                atomicfuLoopBody(konstueType, konstueParameters)
            }
            returnType = irBuiltIns.unitType
        }

        private fun IrSimpleFunction.generateAtomicfuArrayLoop(konstueType: IrType) {
            konst atomicfuArrayClass = atomicSymbols.getAtomicArrayClassByValueType(konstueType)
            addValueParameter(ATOMIC_HANDLER, atomicfuArrayClass.defaultType)
            addValueParameter(INDEX, irBuiltIns.intType)
            addValueParameter(ACTION, atomicSymbols.function1Type(konstueType, irBuiltIns.unitType))
            body = with(atomicSymbols.createBuilder(symbol)) {
                atomicfuArrayLoopBody(atomicfuArrayClass, konstueParameters)
            }
            returnType = irBuiltIns.unitType
        }

        private fun IrSimpleFunction.generateAtomicfuUpdate(functionName: String, konstueType: IrType) {
            addValueParameter(ATOMIC_HANDLER, atomicSymbols.getFieldUpdaterType(konstueType))
            addValueParameter(ACTION, atomicSymbols.function1Type(konstueType, konstueType))
            addValueParameter(DISPATCH_RECEIVER, irBuiltIns.anyNType)
            body = with(atomicSymbols.createBuilder(symbol)) {
                atomicfuUpdateBody(functionName, konstueParameters, konstueType)
            }
            returnType = if (functionName == UPDATE) irBuiltIns.unitType else konstueType
        }

        private fun IrSimpleFunction.generateAtomicfuArrayUpdate(functionName: String, konstueType: IrType) {
            konst atomicfuArrayClass = atomicSymbols.getAtomicArrayClassByValueType(konstueType)
            addValueParameter(ATOMIC_HANDLER, atomicfuArrayClass.defaultType)
            addValueParameter(INDEX, irBuiltIns.intType)
            addValueParameter(ACTION, atomicSymbols.function1Type(konstueType, konstueType))
            body = with(atomicSymbols.createBuilder(symbol)) {
                atomicfuArrayUpdateBody(functionName, atomicfuArrayClass, konstueParameters)
            }
            returnType = if (functionName == UPDATE) irBuiltIns.unitType else konstueType
        }
    }

    private fun getStaticVolatileWrapperInstance(atomicProperty: IrProperty): IrProperty {
        konst volatileWrapperClass = atomicProperty.parent as IrClass
        return (volatileWrapperClass.parent as IrDeclarationContainer).declarations.singleOrNull {
            it is IrProperty && it.backingField != null &&
                    it.backingField!!.type.classOrNull == volatileWrapperClass.symbol
        } as? IrProperty
            ?: error("Static instance of ${volatileWrapperClass.name.asString()} is missing in ${volatileWrapperClass.parent}")
    }

    private fun IrType.isKotlinxAtomicfuPackage() =
        classFqName?.let { it.parent().asString() == AFU_PKG } ?: false

    private fun IrSimpleFunctionSymbol.isKotlinxAtomicfuPackage(): Boolean =
        owner.parentClassOrNull?.classId?.let {
            it.packageFqName.asString() == AFU_PKG
        } ?: false

    private fun IrType.isAtomicValueType() =
        classFqName?.let {
            it.parent().asString() == AFU_PKG && it.shortName().asString() in ATOMIC_VALUE_TYPES
        } ?: false

    private fun IrType.isAtomicArrayType() =
        classFqName?.let {
            it.parent().asString() == AFU_PKG && it.shortName().asString() in ATOMIC_ARRAY_TYPES
        } ?: false

    private fun IrType.isTraceBaseType() =
        classFqName?.let {
            it.parent().asString() == AFU_PKG && it.shortName().asString() == TRACE_BASE_TYPE
        } ?: false

    private fun IrCall.isArrayElementGetter(): Boolean =
        dispatchReceiver?.let {
            it.type.isAtomicArrayType() && symbol.owner.name.asString() == GET
        } ?: false

    private fun IrType.atomicToValueType(): IrType =
        classFqName?.let {
            AFU_VALUE_TYPES[it.shortName().asString()]
        } ?: error("No corresponding konstue type was found for this atomic type: ${this.render()}")

    private fun IrCall.isAtomicFactory(): Boolean =
        symbol.isKotlinxAtomicfuPackage() && symbol.owner.name.asString() == ATOMIC_VALUE_FACTORY &&
                type.isAtomicValueType()

    private fun IrFunction.isAtomicExtension(): Boolean =
        extensionReceiverParameter?.let { it.type.isAtomicValueType() && this.isInline } ?: false

    private fun IrStatement.isTraceCall() = this is IrCall && (isTraceInvoke() || isTraceAppend())

    private fun IrCall.isTraceInvoke(): Boolean =
        symbol.isKotlinxAtomicfuPackage() &&
                symbol.owner.name.asString() == INVOKE &&
                symbol.owner.dispatchReceiverParameter?.type?.isTraceBaseType() == true

    private fun IrCall.isTraceAppend(): Boolean =
        symbol.isKotlinxAtomicfuPackage() &&
                symbol.owner.name.asString() == APPEND &&
                symbol.owner.dispatchReceiverParameter?.type?.isTraceBaseType() == true

    private fun getVolatileWrapperClassName(property: IrProperty) =
        property.name.asString().capitalizeAsciiOnly() + '$' +
                (if (property.parent is IrFile) (property.parent as IrFile).name else property.parent.kotlinFqName.asString()).substringBefore('.') +
                VOLATILE_WRAPPER_SUFFIX

    private fun mangleFunctionName(name: String, isArrayReceiver: Boolean) =
        if (isArrayReceiver) "$name$$ATOMICFU$ATOMIC_ARRAY_RECEIVER_SUFFIX" else "$name$$ATOMICFU"
}
