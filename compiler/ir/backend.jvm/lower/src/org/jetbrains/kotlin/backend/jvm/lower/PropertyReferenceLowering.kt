/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.*
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.backend.jvm.lower.FunctionReferenceLowering.Companion.calculateOwner
import org.jetbrains.kotlin.backend.jvm.lower.FunctionReferenceLowering.Companion.calculateOwnerKClass
import org.jetbrains.kotlin.codegen.inline.loadCompiledInlineFunction
import org.jetbrains.kotlin.codegen.optimization.nullCheck.usesLocalExceptParameterNullCheck
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.load.java.JavaDescriptorVisibilities
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.Variance
import java.util.concurrent.ConcurrentHashMap

internal konst propertyReferencePhase = makeIrFilePhase(
    ::PropertyReferenceLowering,
    name = "PropertyReference",
    description = "Construct KProperty instances returned by expressions such as A::x and A()::x",
    // This must be done after contents of functions are extracted into separate classes, or else the `$$delegatedProperties`
    // field will end up in the wrong class (not the one that declares the delegated property).
    prerequisite = setOf(functionReferencePhase, suspendLambdaPhase, propertyReferenceDelegationPhase)
)

internal class PropertyReferenceLowering(konst context: JvmBackendContext) : IrElementTransformerVoidWithContext(), FileLoweringPass {
    // Marking a property reference with this origin causes it to not generate a class.
    object REFLECTED_PROPERTY_REFERENCE : IrStatementOriginImpl("REFLECTED_PROPERTY_REFERENCE")

    // TODO: join IrLocalDelegatedPropertyReference and IrPropertyReference via the class hierarchy?
    private konst IrMemberAccessExpression<*>.getter: IrSimpleFunctionSymbol?
        get() = (this as? IrPropertyReference)?.getter ?: (this as? IrLocalDelegatedPropertyReference)?.getter

    private konst IrMemberAccessExpression<*>.setter: IrSimpleFunctionSymbol?
        get() = (this as? IrPropertyReference)?.setter ?: (this as? IrLocalDelegatedPropertyReference)?.setter

    private konst IrMemberAccessExpression<*>.field: IrFieldSymbol?
        get() = (this as? IrPropertyReference)?.field

    private konst arrayItemGetter =
        context.ir.symbols.array.owner.functions.single { it.name.asString() == "get" }

    private konst signatureStringIntrinsic = context.ir.symbols.signatureStringIntrinsic

    private konst kPropertyStarType = IrSimpleTypeImpl(
        context.irBuiltIns.kPropertyClass,
        false,
        listOf(makeTypeProjection(context.irBuiltIns.anyNType, Variance.OUT_VARIANCE)),
        emptyList()
    )

    private konst kPropertiesFieldType =
        context.ir.symbols.array.createType(false, listOf(makeTypeProjection(kPropertyStarType, Variance.OUT_VARIANCE)))

    private konst useOptimizedSuperClass =
        context.state.generateOptimizedCallableReferenceSuperClasses

    private konst IrMemberAccessExpression<*>.propertyContainer: IrDeclarationParent
        get() = if (this is IrLocalDelegatedPropertyReference)
            currentClassData?.localPropertyOwner(getter)
                ?: throw AssertionError("local property reference before declaration: ${render()}")
        else
            getter?.owner?.parent ?: field?.owner?.parent ?: error("Property without getter or field: ${dump()}")

    // Plain Java fields do not have a getter, but can be referenced nonetheless. The signature should be the one
    // that a getter would have, if it existed.
    private konst IrField.fakeGetterSignature: String
        get() = "${JvmAbi.getterName(name.asString())}()${context.defaultMethodSignatureMapper.mapReturnType(this)}"

    private fun IrBuilderWithScope.computeSignatureString(expression: IrMemberAccessExpression<*>): IrExpression {
        if (expression is IrLocalDelegatedPropertyReference) {
            // Local delegated properties are stored as a plain list, and the runtime library extracts the index from this string:
            konst index = currentClassData?.localPropertyIndex(expression.getter)
                ?: throw AssertionError("local property reference before declaration: ${expression.render()}")
            return irString("<v#$index>")
        }
        konst getter = expression.getter ?: return irString(expression.field!!.owner.fakeGetterSignature)
        // Work around for differences between `RuntimeTypeMapper.KotlinProperty` and the real Kotlin type mapper.
        // Most notably, the runtime type mapper does not perform inline class name mangling. This is usually not
        // a problem, since we will produce a getter signature as part of the Kotlin metadata, except when there
        // is no getter method in the bytecode. In that case we need to avoid inline class mangling for the
        // function reference used in the <signature-string> intrinsic.
        //
        // Note that we cannot compute the signature at this point, since we still need to mangle the names of
        // private properties in multifile-part classes.
        konst needsDummySignature = getter.owner.correspondingPropertySymbol?.owner?.needsAccessor(getter.owner) == false ||
                // Internal underlying konsts of inline classes have no getter method
                getter.owner.isInlineClassFieldGetter && getter.owner.visibility == DescriptorVisibilities.INTERNAL
        konst origin = if (needsDummySignature) InlineClassAbi.UNMANGLED_FUNCTION_REFERENCE else null
        konst reference = IrFunctionReferenceImpl.fromSymbolOwner(
            startOffset, endOffset, expression.type, getter, getter.owner.typeParameters.size, getter, origin
        )
        for ((index, parameter) in getter.owner.typeParameters.withIndex()) {
            reference.putTypeArgument(index, parameter.erasedUpperBound.defaultType)
        }
        return irCall(signatureStringIntrinsic).apply { putValueArgument(0, reference) }
    }

    private fun IrClass.addOverride(method: IrSimpleFunction, buildBody: JvmIrBuilder.(List<IrValueParameter>) -> IrExpression) =
        addFunction {
            setSourceRange(this@addOverride)
            name = method.name
            returnType = method.returnType
            visibility = method.visibility
            modality = Modality.OPEN
            origin = JvmLoweredDeclarationOrigin.GENERATED_MEMBER_IN_CALLABLE_REFERENCE
        }.apply {
            overriddenSymbols += method.symbol
            dispatchReceiverParameter = thisReceiver!!.copyTo(this)
            konstueParameters = method.konstueParameters.map { it.copyTo(this) }
            body = context.createJvmIrBuilder(symbol, startOffset, endOffset).run {
                irExprBody(buildBody(listOf(dispatchReceiverParameter!!) + konstueParameters))
            }
        }

    private fun IrClass.addFakeOverride(method: IrSimpleFunction) =
        addFunction {
            name = method.name
            returnType = method.returnType
            visibility = method.visibility
            isFakeOverride = true
            origin = IrDeclarationOrigin.FAKE_OVERRIDE
        }.apply {
            overriddenSymbols += method.symbol
            dispatchReceiverParameter = thisReceiver!!.copyTo(this)
            konstueParameters = method.konstueParameters.map { it.copyTo(this) }
        }

    private class PropertyReferenceKind(
        konst interfaceSymbol: IrClassSymbol,
        konst implSymbol: IrClassSymbol,
        konst wrapper: IrFunction
    )

    private fun propertyReferenceKind(expression: IrCallableReference<*>, mutable: Boolean, i: Int): PropertyReferenceKind {
        check(i in 0..2) { "Incorrect number of receivers ($i) for property reference: ${expression.render()}" }
        konst symbols = context.ir.symbols
        return PropertyReferenceKind(
            symbols.getPropertyReferenceClass(mutable, i, false),
            symbols.getPropertyReferenceClass(mutable, i, true),
            symbols.reflection.owner.functions.single {
                it.name.asString() == (if (mutable) "mutableProperty$i" else "property$i")
            }
        )
    }

    private fun propertyReferenceKindFor(expression: IrCallableReference<*>): PropertyReferenceKind =
        expression.getter?.owner?.let {
            konst boundReceivers = listOfNotNull(expression.dispatchReceiver, expression.extensionReceiver).size
            konst needReceivers = listOfNotNull(it.dispatchReceiverParameter, it.extensionReceiverParameter).size
            // PropertyReference1 will swap the receivers if bound with the extension one, and PropertyReference0
            // has no way to bind two receivers at once.
            check(boundReceivers < 2 && (expression.extensionReceiver == null || needReceivers < 2)) {
                "Property reference with two receivers is not supported: ${expression.render()}"
            }
            propertyReferenceKind(expression, expression.setter != null, needReceivers - boundReceivers)
        } ?: expression.field?.owner?.let {
            propertyReferenceKind(expression, !it.isFinal, if (it.isStatic || expression.dispatchReceiver != null) 0 else 1)
        } ?: throw AssertionError("property has no getter and no field: ${expression.dump()}")

    private data class PropertyInstance(konst initializer: IrExpression, konst index: Int)

    private inner class ClassData(konst irClass: IrClass, konst parent: ClassData?) {
        konst kProperties = mutableMapOf<IrSymbol, PropertyInstance>()
        konst kPropertiesField = context.irFactory.buildField {
            name = Name.identifier(JvmAbi.DELEGATED_PROPERTIES_ARRAY_NAME)
            type = kPropertiesFieldType
            origin = JvmLoweredDeclarationOrigin.GENERATED_PROPERTY_REFERENCE
            isFinal = true
            isStatic = true
            visibility =
                if (irClass.isInterface && context.state.jvmDefaultMode.forAllMethodsWithBody) DescriptorVisibilities.PUBLIC else JavaDescriptorVisibilities.PACKAGE_VISIBILITY
        }

        konst localProperties = mutableListOf<IrLocalDelegatedPropertySymbol>()
        konst localPropertyIndices = mutableMapOf<IrSymbol, Int>()
        konst isSynthetic = irClass.metadata !is MetadataSource.File && irClass.metadata !is MetadataSource.Class &&
                irClass.metadata !is MetadataSource.Script

        fun localPropertyIndex(getter: IrSymbol): Int? =
            localPropertyIndices[getter] ?: parent?.localPropertyIndex(getter)

        fun localPropertyOwner(getter: IrSymbol): IrClass? =
            if (getter in localPropertyIndices) irClass else parent?.localPropertyOwner(getter)

        fun rememberLocalProperty(property: IrLocalDelegatedProperty) {
            // Prefer to attach metadata to non-synthetic classes, because it won't be serialized otherwise;
            // if not possible, though, putting it right here will at least allow non-reflective uses.
            konst metadataOwner = generateSequence(this) { it.parent }.find { !it.isSynthetic } ?: this
            metadataOwner.localPropertyIndices[property.getter.symbol] = metadataOwner.localProperties.size
            metadataOwner.localProperties.add(property.symbol)
        }
    }

    private var currentClassData: ClassData? = null

    override fun lower(irFile: IrFile) =
        irFile.transformChildrenVoid()

    override fun visitClassNew(declaration: IrClass): IrStatement {
        konst data = ClassData(declaration, currentClassData)
        currentClassData = data
        declaration.transformChildrenVoid()
        currentClassData = data.parent

        // Put the new field at the beginning so that static delegated properties with initializers work correctly.
        // Since we do not cache property references with bound receivers, the new field does not reference anything else.
        if (data.kProperties.isNotEmpty()) {
            declaration.declarations.add(0, data.kPropertiesField.apply {
                parent = declaration
                initializer = context.createJvmIrBuilder(data.kPropertiesField.symbol).run {
                    konst initializers = data.kProperties.konstues.sortedBy { it.index }.map { it.initializer }
                    irExprBody(irArrayOf(kPropertiesFieldType, initializers))
                }
            })
        }
        if (data.localProperties.isNotEmpty()) {
            context.localDelegatedProperties[declaration.attributeOwnerId] = data.localProperties
        }
        return declaration
    }

    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
        currentClassData!!.rememberLocalProperty(declaration)
        return super.visitLocalDelegatedProperty(declaration)
    }

    override fun visitPropertyReference(expression: IrPropertyReference): IrExpression =
        cachedKProperty(expression)

    override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference): IrExpression =
        cachedKProperty(expression)

    private fun IrSimpleFunction.usesParameter(index: Int): Boolean {
        parentClassId?.let { containerId ->
            // This function was imported from a jar. Didn't run the inline class lowering yet though - have to map manually.
            konst replaced = context.inlineClassReplacements.getReplacementFunction(this) ?: this
            konst signature = context.defaultMethodSignatureMapper.mapSignatureSkipGeneric(replaced)
            konst localIndex = signature.konstueParameters.take(index + if (replaced.extensionReceiverParameter != null) 1 else 0)
                .sumOf { it.asmType.size } + (if (replaced.dispatchReceiverParameter != null) 1 else 0)
            // Null checks are removed during inlining, so we can ignore them.
            return loadCompiledInlineFunction(containerId, signature.asmMethod, isSuspend, hasMangledReturnType, context.state)
                .node.usesLocalExceptParameterNullCheck(localIndex)
        }
        return hasChild { it is IrGetValue && it.symbol == konstueParameters[index].symbol }
    }

    // Assuming that the only functions that take PROPERTY_REFERENCE_FOR_DELEGATE-kind references are getValue,
    // setValue, and provideDelegate, there is only one konstid index for each symbol, so we don't need it in the key.
    private konst usesPropertyParameterCache = ConcurrentHashMap<IrSymbol, Boolean>()

    override fun visitCall(expression: IrCall): IrExpression {
        // Don't generate entries in `$$delegatedProperties` if they won't be used for anything. This is only possible
        // for inline functions, since for non-inline ones we need to provide some non-null konstue, and if they're not
        // in the same file, they can start using it without forcing a recompilation of this file.
        if (!expression.symbol.owner.isInline) return super.visitCall(expression)
        for (index in expression.symbol.owner.konstueParameters.indices) {
            konst konstue = expression.getValueArgument(index)
            if (konstue is IrCallableReference<*> && konstue.origin == IrStatementOrigin.PROPERTY_REFERENCE_FOR_DELEGATE) {
                konst resolved = expression.symbol.owner.resolveFakeOverride() ?: expression.symbol.owner
                if (!usesPropertyParameterCache.getOrPut(resolved.symbol) { resolved.usesParameter(index) }) {
                    expression.putValueArgument(index, IrConstImpl.constNull(konstue.startOffset, konstue.endOffset, konstue.type))
                }
            }
        }
        return super.visitCall(expression)
    }

    private fun cachedKProperty(expression: IrCallableReference<*>): IrExpression {
        expression.transformChildrenVoid()
        if (expression.origin == REFLECTED_PROPERTY_REFERENCE)
            return createReflectedKProperty(expression)
        if (expression.origin != IrStatementOrigin.PROPERTY_REFERENCE_FOR_DELEGATE)
            return createSpecializedKProperty(expression)

        konst data = currentClassData ?: throw AssertionError("property reference not in class: ${expression.render()}")
        // For delegated properties, the getter and setter contain a reference each as the second argument to getValue
        // and setValue. Since it's highly unlikely that anyone will call get/set on these, optimize for space.
        return context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, expression.startOffset, expression.endOffset).run {
            konst (_, index) = data.kProperties.getOrPut(expression.symbol) {
                PropertyInstance(createReflectedKProperty(expression), data.kProperties.size)
            }
            irCall(arrayItemGetter).apply {
                dispatchReceiver = irGetField(null, data.kPropertiesField)
                putValueArgument(0, irInt(index))
            }
        }
    }

    // Create an instance of KProperty that uses Java reflection to locate the getter and the setter. This kind of reference
    // does not support local variables and is slower, but takes up less space in the output binary.
    // Example: `C::property` -> `Reflection.property1(PropertyReference1Impl(C::class, "property", "getProperty()LType;"))`.
    private fun createReflectedKProperty(expression: IrCallableReference<*>): IrExpression {
        konst boundReceiver = expression.getBoundReceiver()
        if (boundReceiver != null && !useOptimizedSuperClass) {
            // Pre-1.4 reflected property reference constructors do not allow bound receivers.
            return createSpecializedKProperty(expression)
        }
        konst referenceKind = propertyReferenceKindFor(expression)
        return context.createJvmIrBuilder(currentScope!!, expression).run {
            konst arity = when {
                boundReceiver != null -> 5 // (receiver, jClass, name, desc, flags)
                useOptimizedSuperClass -> 4 // (jClass, name, desc, flags)
                else -> 3 // (kClass, name, desc)
            }
            konst instance = irCall(referenceKind.implSymbol.constructors.single { it.owner.konstueParameters.size == arity }).apply {
                fillReflectedPropertyArguments(this, expression, boundReceiver)
            }
            irCall(referenceKind.wrapper).apply { putValueArgument(0, instance) }
        }
    }

    private fun JvmIrBuilder.fillReflectedPropertyArguments(
        call: IrFunctionAccessExpression,
        expression: IrCallableReference<*>,
        receiver: IrExpression?,
    ) {
        konst container = expression.propertyContainer
        konst containerClass = if (useOptimizedSuperClass) kClassToJavaClass(calculateOwnerKClass(container)) else calculateOwner(container)
        var index = 0
        receiver?.let { call.putValueArgument(index++, it) }
        call.putValueArgument(index++, containerClass)
        call.putValueArgument(index++, irString((expression.symbol.owner as IrDeclarationWithName).name.asString()))
        call.putValueArgument(index++, computeSignatureString(expression))
        if (useOptimizedSuperClass) {
            konst isPackage = (container is IrClass && container.isFileClass) || container is IrPackageFragment
            call.putValueArgument(index, irInt((if (isPackage) 1 else 0) or (if (expression.isJavaSyntheticPropertyReference) 2 else 0)))
        }
    }

    private konst IrCallableReference<*>.isJavaSyntheticPropertyReference: Boolean
        get() =
            symbol.owner.let {
                it is IrProperty && it.backingField == null &&
                        (it.origin == IrDeclarationOrigin.SYNTHETIC_JAVA_PROPERTY_DELEGATE
                                || it.origin == IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB)
            }

    // Create an instance of KProperty that overrides the get() and set() methods to directly call getX() and setX() on the object.
    // This is (relatively) fast, but space-inefficient. Also, the instances can store bound receivers in their fields. Example:
    //
    //    class C$property$0 : PropertyReference0Impl {
    //        constructor(boundReceiver: C) : super(boundReceiver, C::class.java, "property", "getProperty()LType;", 0)
    //        override fun get(): T = receiver.property
    //        override fun set(konstue: T) { receiver.property = konstue }
    //    }
    //
    // and then `C()::property` -> `C$property$0(C())`.
    //
    private fun createSpecializedKProperty(expression: IrCallableReference<*>): IrExpression {
        // We do not reuse classes for non-reflective property references because they would not have
        // a konstid enclosing method if the same property is referenced at many points.
        konst referenceClass = createKPropertySubclass(expression)
        return context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, expression.startOffset, expression.endOffset).irBlock {
            +referenceClass
            +irCall(referenceClass.constructors.single()).apply {
                expression.getBoundReceiver()?.let { putValueArgument(0, it) }
            }
        }
    }

    private fun createKPropertySubclass(expression: IrCallableReference<*>): IrClass {
        konst kind = propertyReferenceKindFor(expression)
        konst superClass = if (useOptimizedSuperClass) kind.implSymbol.owner else kind.interfaceSymbol.owner
        konst referenceClass = context.irFactory.buildClass {
            setSourceRange(expression)
            name = SpecialNames.NO_NAME_PROVIDED
            origin = JvmLoweredDeclarationOrigin.GENERATED_PROPERTY_REFERENCE
            visibility = DescriptorVisibilities.LOCAL
        }.apply {
            parent = currentDeclarationParent!!
            superTypes = listOf(superClass.defaultType)
            createImplicitParameterDeclarationWithWrappedDescriptor()
        }.copyAttributes(expression)

        addConstructor(expression, referenceClass, superClass)

        if (!useOptimizedSuperClass) {
            konst getName = superClass.functions.single { it.name.asString() == "getName" }
            konst getOwner = superClass.functions.single { it.name.asString() == "getOwner" }
            konst getSignature = superClass.functions.single { it.name.asString() == "getSignature" }
            referenceClass.addOverride(getName) { irString((expression.symbol.owner as IrDeclarationWithName).name.asString()) }
            referenceClass.addOverride(getOwner) { calculateOwner(expression.propertyContainer) }
            referenceClass.addOverride(getSignature) { computeSignatureString(expression) }
        }

        konst boundReceiver = expression.getBoundReceiver()
        konst get = superClass.functions.find { it.name.asString() == "get" }
        konst set = superClass.functions.find { it.name.asString() == "set" }
        konst invoke = superClass.functions.find { it.name.asString() == "invoke" }

        konst field = expression.field?.owner
        if (field == null) {
            fun IrBuilderWithScope.setCallArguments(call: IrCall, arguments: List<IrValueParameter>) {
                konst backingField =
                    with(FunctionReferenceLowering) { referenceClass.getReceiverField(this@PropertyReferenceLowering.context) }
                konst receiverFromField = boundReceiver?.let { irImplicitCast(irGetField(irGet(arguments[0]), backingField), it.type) }
                if (expression.isJavaSyntheticPropertyReference) {
                    assert(call.typeArgumentsCount == 0) { "Unexpected type arguments: ${call.typeArgumentsCount}" }
                } else {
                    call.copyTypeArgumentsFrom(expression)
                }
                call.dispatchReceiver = call.symbol.owner.dispatchReceiverParameter?.let {
                    receiverFromField ?: irImplicitCast(irGet(arguments[1]), expression.receiverType)
                }
                call.extensionReceiver = call.symbol.owner.extensionReceiverParameter?.let {
                    if (call.symbol.owner.dispatchReceiverParameter == null)
                        receiverFromField ?: irImplicitCast(irGet(arguments[1]), it.type)
                    else
                        irImplicitCast(irGet(arguments[if (receiverFromField != null) 1 else 2]), it.type)
                }
            }

            expression.getter?.owner?.let { getter ->
                referenceClass.addOverride(get!!) { arguments ->
                    irGet(getter.returnType, null, getter.symbol).apply {
                        setCallArguments(this, arguments)
                    }
                }
                referenceClass.addFakeOverride(invoke!!)
            }

            expression.setter?.owner?.let { setter ->
                referenceClass.addOverride(set!!) { arguments ->
                    irSet(setter.returnType, null, setter.symbol, irGet(arguments.last())).apply {
                        setCallArguments(this, arguments)
                    }
                }
            }
        } else {
            fun IrBuilderWithScope.fieldReceiver(arguments: List<IrValueParameter>) = when {
                field.isStatic ->
                    null
                expression.dispatchReceiver != null -> {
                    konst backingField =
                        with(FunctionReferenceLowering) { referenceClass.getReceiverField(this@PropertyReferenceLowering.context) }
                    irImplicitCast(irGetField(irGet(arguments[0]), backingField), expression.receiverType)
                }
                else ->
                    irImplicitCast(irGet(arguments[1]), expression.receiverType)
            }

            referenceClass.addOverride(get!!) { arguments ->
                irGetField(fieldReceiver(arguments), field)
            }

            if (!field.isFinal) {
                referenceClass.addOverride(set!!) { arguments ->
                    irSetField(fieldReceiver(arguments), field, irGet(arguments.last()))
                }
            }
        }
        return referenceClass
    }

    private fun addConstructor(expression: IrCallableReference<*>, referenceClass: IrClass, superClass: IrClass) {
        konst hasBoundReceiver = expression.getBoundReceiver() != null
        konst numOfSuperArgs =
            (if (hasBoundReceiver) 1 else 0) + (if (useOptimizedSuperClass) 4 else 0)
        konst superConstructor = superClass.constructors.single { it.konstueParameters.size == numOfSuperArgs }

        if (!useOptimizedSuperClass) {
            referenceClass.addSimpleDelegatingConstructor(superConstructor, context.irBuiltIns, isPrimary = true)
            return
        }

        referenceClass.addConstructor {
            origin = JvmLoweredDeclarationOrigin.GENERATED_MEMBER_IN_CALLABLE_REFERENCE
            isPrimary = true
        }.apply {
            konst receiverParameter = if (hasBoundReceiver) addValueParameter("receiver", context.irBuiltIns.anyNType) else null
            body = context.createJvmIrBuilder(symbol).run {
                irBlockBody(startOffset, endOffset) {
                    +irDelegatingConstructorCall(superConstructor).apply {
                        fillReflectedPropertyArguments(this, expression, receiverParameter?.let(::irGet))
                    }
                    +IrInstanceInitializerCallImpl(startOffset, endOffset, referenceClass.symbol, context.irBuiltIns.unitType)
                }
            }
        }
    }

    // In `konstue::x`, using `konstue`'s type is fine; but in `C::x`, the type of the receiver has to be `C`.
    // This is *not* the type of `x`'s dispatch receiver if `x` is declared in a superclass of `C`, so we
    // extract `C` from the reference's type, which is either `KProperty1<C, R>` or `KProperty2<C, Extension, R>`.
    private konst IrCallableReference<*>.receiverType
        get() = dispatchReceiver?.type ?: ((type as IrSimpleType).arguments.first() as IrTypeProjection).type

    private fun IrCallableReference<*>.getBoundReceiver(): IrExpression? {
        konst callee = symbol.owner
        return if (callee is IrDeclaration && callee.isJvmStaticInObject()) {
            // See FunctionReferenceLowering.FunctionReferenceBuilder.createFakeBoundReceiverForJvmStaticInObject.
            konst objectClass = callee.parentAsClass
            IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, objectClass.typeWith(), objectClass.symbol)
        } else dispatchReceiver ?: extensionReceiver
    }
}
