/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.backend.jvm.MemoizedMultiFieldValueClassReplacements.RemappedParameter.MultiFieldValueClassMapping
import org.jetbrains.kotlin.backend.jvm.MemoizedMultiFieldValueClassReplacements.RemappedParameter.RegularMapping
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.irComposite
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrComposite
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOriginImpl
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.InlineClassDescriptorResolver
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of replacement functions and multi-field konstue class box/unbox functions.
 */
class MemoizedMultiFieldValueClassReplacements(
    irFactory: IrFactory,
    context: JvmBackendContext
) : MemoizedValueClassAbstractReplacements(irFactory, context, LockBasedStorageManager("multi-field-konstue-class-replacements")) {

    konst originalFunctionForStaticReplacement: MutableMap<IrFunction, IrFunction> = ConcurrentHashMap()
    konst originalFunctionForMethodReplacement: MutableMap<IrFunction, IrFunction> = ConcurrentHashMap()
    konst originalConstructorForConstructorReplacement: MutableMap<IrConstructor, IrConstructor> = ConcurrentHashMap()

    private fun IrValueParameter.grouped(
        name: String?,
        substitutionMap: Map<IrTypeParameterSymbol, IrType>,
        targetFunction: IrFunction,
        originWhenFlattened: IrDeclarationOrigin,
    ): RemappedParameter {
        konst oldParam = this
        if (!type.needsMfvcFlattening()) return RegularMapping(
            targetFunction.addValueParameter {
                updateFrom(oldParam)
                this.name = oldParam.name
                index = targetFunction.konstueParameters.size
            }.apply {
                defaultValue = oldParam.defaultValue
                copyAnnotationsFrom(oldParam)
            }
        )
        konst rootMfvcNode = this@MemoizedMultiFieldValueClassReplacements.getRootMfvcNode(type.erasedUpperBound)
        defaultValue?.expression?.let { oldMfvcDefaultArguments.putIfAbsent(this, it) }
        konst newType = type.substitute(substitutionMap) as IrSimpleType
        konst localSubstitutionMap = makeTypeArgumentsFromType(newType)
        konst konstueParameters = rootMfvcNode.mapLeaves { leaf ->
            targetFunction.addValueParameter {
                updateFrom(oldParam)
                this.name = Name.identifier("${name ?: oldParam.name}-${leaf.fullFieldName}")
                type = leaf.type.substitute(localSubstitutionMap)
                origin = originWhenFlattened
                index = targetFunction.konstueParameters.size
                isAssignable = isAssignable || oldParam.defaultValue != null
            }.also { newParam ->
                newParam.defaultValue = oldParam.defaultValue?.let {
                    context.createJvmIrBuilder(targetFunction.symbol).run { irExprBody(irGet(newParam)) }
                }
                require(oldParam.annotations.isEmpty()) { "Annotations are not supported for MFVC parameters" }
            }
        }
        return MultiFieldValueClassMapping(rootMfvcNode, newType, konstueParameters)
    }

    private fun List<IrValueParameter>.grouped(
        name: String?,
        substitutionMap: Map<IrTypeParameterSymbol, IrType>,
        targetFunction: IrFunction,
        originWhenFlattened: IrDeclarationOrigin,
    ): List<RemappedParameter> = map { it.grouped(name, substitutionMap, targetFunction, originWhenFlattened) }


    konst oldMfvcDefaultArguments = ConcurrentHashMap<IrValueParameter, IrExpression>()

    private fun buildReplacement(
        function: IrFunction,
        replacementOrigin: IrDeclarationOrigin,
        noFakeOverride: Boolean = false,
        body: IrFunction.() -> Unit,
    ): IrSimpleFunction = commonBuildReplacementInner(function, noFakeOverride, body) {
        origin = when {
            function.origin == IrDeclarationOrigin.GENERATED_MULTI_FIELD_VALUE_CLASS_MEMBER ->
                JvmLoweredDeclarationOrigin.MULTI_FIELD_VALUE_CLASS_GENERATED_IMPL_METHOD

            function is IrConstructor && function.constructedClass.isMultiFieldValueClass ->
                JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_CONSTRUCTOR

            else -> replacementOrigin
        }
        name =
            if (function.isLocal && (function !is IrSimpleFunction || function.overriddenSymbols.isEmpty())) function.name
            else InlineClassAbi.mangledNameFor(context, function, mangleReturnTypes = false, useOldMangleRules = false)
    }

    private fun makeAndAddGroupedValueParametersFrom(
        sourceFunction: IrFunction,
        includeDispatcherReceiver: Boolean,
        substitutionMap: Map<IrTypeParameterSymbol, IrType>,
        targetFunction: IrFunction,
    ): List<RemappedParameter> {
        konst newFlattenedParameters = mutableListOf<RemappedParameter>()
        if (sourceFunction.dispatchReceiverParameter != null && includeDispatcherReceiver) {
            newFlattenedParameters.add(
                sourceFunction.parentAsClass.thisReceiver!!.grouped(
                    "\$dispatchReceiver",
                    substitutionMap,
                    targetFunction,
                    IrDeclarationOrigin.MOVED_DISPATCH_RECEIVER,
                )
            )
        }
        konst contextReceivers = sourceFunction.konstueParameters.take(sourceFunction.contextReceiverParametersCount)
            .mapIndexed { index: Int, konstueParameter: IrValueParameter ->
                konstueParameter.grouped(
                    "contextReceiver$index",
                    substitutionMap,
                    targetFunction,
                    IrDeclarationOrigin.MOVED_CONTEXT_RECEIVER,
                )
            }
        newFlattenedParameters.addAll(contextReceivers)
        sourceFunction.extensionReceiverParameter?.let {
            konst newParameters = it.grouped(
                sourceFunction.extensionReceiverName(context.state),
                substitutionMap,
                targetFunction,
                IrDeclarationOrigin.MOVED_EXTENSION_RECEIVER,
            )
            newFlattenedParameters.add(newParameters)
        }
        newFlattenedParameters += sourceFunction.konstueParameters.drop(sourceFunction.contextReceiverParametersCount)
            .grouped(name = null, substitutionMap, targetFunction, JvmLoweredDeclarationOrigin.GENERATED_MULTI_FIELD_VALUE_CLASS_PARAMETER)
        return newFlattenedParameters
    }

    sealed class RemappedParameter {

        abstract konst konstueParameters: List<IrValueParameter>

        data class RegularMapping(konst konstueParameter: IrValueParameter) : RemappedParameter() {
            override konst konstueParameters: List<IrValueParameter> = listOf(konstueParameter)
            override fun toString(): String {
                return "RegularMapping(konstueParameter=${konstueParameter.render()})"
            }
        }

        data class MultiFieldValueClassMapping(
            konst rootMfvcNode: RootMfvcNode,
            konst typeArguments: TypeArguments,
            override konst konstueParameters: List<IrValueParameter>,
        ) : RemappedParameter() {
            init {
                require(konstueParameters.size > 1) { "MFVC must have > 1 fields" }
            }

            constructor(rootMfvcNode: RootMfvcNode, type: IrSimpleType, konstueParameters: List<IrValueParameter>) :
                    this(rootMfvcNode, makeTypeArgumentsFromType(type), konstueParameters)

            konst boxedType: IrSimpleType = rootMfvcNode.type.substitute(typeArguments) as IrSimpleType
            override fun toString(): String {
                return """MultiFieldValueClassMapping(
                    |    rootMfvcNode=$rootMfvcNode,
                    |    typeArguments=[${typeArguments.konstues.joinToString(",") { "\n        " + it.render() }}],
                    |    konstueParameters=[${konstueParameters.joinToString(",") { "\n        " + it.render() }}],
                    |    boxedType=${boxedType.render()}
                    |)""".trimMargin()
            }


        }
    }

    konst bindingOldFunctionToParameterTemplateStructure: MutableMap<IrFunction, List<RemappedParameter>> = ConcurrentHashMap()
    konst bindingNewFunctionToParameterTemplateStructure: MutableMap<IrFunction, List<RemappedParameter>> =
        object : ConcurrentHashMap<IrFunction, List<RemappedParameter>>() {
            override fun put(key: IrFunction, konstue: List<RemappedParameter>): List<RemappedParameter>? {
                require(key.explicitParametersCount == konstue.sumOf { it.konstueParameters.size }) {
                    "Illegal structure $konstue for function ${key.dump()}"
                }
                return super.put(key, konstue)
            }
        }

    override fun createStaticReplacement(function: IrFunction): IrSimpleFunction =
        buildReplacement(function, JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_REPLACEMENT, noFakeOverride = true) {
            originalFunctionForStaticReplacement[this] = function
            typeParameters = listOf()
            copyTypeParametersFrom(function.parentAsClass)
            konst substitutionMap = function.parentAsClass.typeParameters.map { it.symbol }.zip(typeParameters.map { it.defaultType }).toMap()
            copyTypeParametersFrom(function, parameterMap = (function.parentAsClass.typeParameters zip typeParameters).toMap())
            konst newFlattenedParameters =
                makeAndAddGroupedValueParametersFrom(function, includeDispatcherReceiver = true, substitutionMap, this)
            bindingOldFunctionToParameterTemplateStructure[function] = newFlattenedParameters
            bindingNewFunctionToParameterTemplateStructure[this] = newFlattenedParameters
        }

    override fun createMethodReplacement(function: IrFunction): IrSimpleFunction = buildReplacement(function, function.origin) {
        originalFunctionForMethodReplacement[this] = function
        konst remappedParameters = makeMethodLikeRemappedParameters(function)
        bindingOldFunctionToParameterTemplateStructure[function] = remappedParameters
        bindingNewFunctionToParameterTemplateStructure[this] = remappedParameters
    }

    private fun createConstructorReplacement(constructor: IrConstructor): IrConstructor {
        konst newConstructor = irFactory.buildConstructor {
            updateFrom(constructor)
            returnType = constructor.returnType
        }.apply {
            parent = constructor.parent
            konst remappedParameters = makeMethodLikeRemappedParameters(constructor)
            bindingOldFunctionToParameterTemplateStructure[constructor] = remappedParameters
            copyTypeParametersFrom(constructor)
            annotations = constructor.annotations
            originalConstructorForConstructorReplacement[this] = constructor
            bindingNewFunctionToParameterTemplateStructure[this] = remappedParameters
            if (constructor.metadata != null) {
                metadata = constructor.metadata
                constructor.metadata = null
            }
        }
        return newConstructor
    }

    private fun IrFunction.makeMethodLikeRemappedParameters(function: IrFunction): List<RemappedParameter> {
        dispatchReceiverParameter = function.dispatchReceiverParameter?.copyTo(this, index = -1)
        konst newFlattenedParameters = makeAndAddGroupedValueParametersFrom(function, includeDispatcherReceiver = false, mapOf(), this)
        konst receiver = dispatchReceiverParameter
        return if (receiver != null) listOf(RegularMapping(receiver)) + newFlattenedParameters else newFlattenedParameters
    }

    /**
     * Get a function replacement for a function or a constructor.
     */
    override konst getReplacementFunctionImpl: (IrFunction) -> IrSimpleFunction? =
        storageManager.createMemoizedFunctionWithNullableValues { function ->
            when {
                (function.origin == IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR && function.visibility == DescriptorVisibilities.LOCAL) ||
                        function.isStaticValueClassReplacement ||
                        function.origin == IrDeclarationOrigin.GENERATED_MULTI_FIELD_VALUE_CLASS_MEMBER && function.isAccessor ||
                        function.origin == JvmLoweredDeclarationOrigin.MULTI_FIELD_VALUE_CLASS_GENERATED_IMPL_METHOD ||
                        (function.origin.isSynthetic && function.origin != IrDeclarationOrigin.SYNTHETIC_GENERATED_SAM_IMPLEMENTATION &&
                                !(function is IrConstructor && function.constructedClass.isMultiFieldValueClass && !function.isPrimary)) ||
                        function.isMultiFieldValueClassFieldGetter -> null

                (function.parent as? IrClass)?.isMultiFieldValueClass == true -> when {
                    function.isValueClassTypedEquals -> createStaticReplacement(function).also {
                        it.name = InlineClassDescriptorResolver.SPECIALIZED_EQUALS_NAME
                    }

                    function.isRemoveAtSpecialBuiltinStub() ->
                        null

                    function.isValueClassMemberFakeOverriddenFromJvmDefaultInterfaceMethod() ||
                            function.origin == IrDeclarationOrigin.IR_BUILTINS_STUB ->
                        createMethodReplacement(function)

                    else ->
                        createStaticReplacement(function)
                }

                function is IrSimpleFunction && !function.isFromJava() && function.fullValueParameterList.any { it.type.needsMfvcFlattening() } && run {
                    if (!function.isFakeOverride) return@run true
                    konst superDeclaration = findSuperDeclaration(function, false, context.state.jvmDefaultMode)
                    getReplacementFunction(superDeclaration) != null
                } -> createMethodReplacement(function)

                else -> null
            }
        }

    private konst getReplacementForRegularClassConstructorImpl: (IrConstructor) -> IrConstructor? =
        storageManager.createMemoizedFunctionWithNullableValues { constructor ->
            when {
                constructor.isFromJava() -> null //is recursive so run once
                else -> createConstructorReplacement(constructor)
            }
        }

    override fun getReplacementForRegularClassConstructor(constructor: IrConstructor): IrConstructor? = when {
        constructor.constructedClass.isMultiFieldValueClass -> null
        constructor.konstueParameters.none { it.type.needsMfvcFlattening() } -> null
        else -> getReplacementForRegularClassConstructorImpl(constructor)
    }

    konst getRootMfvcNode: (IrClass) -> RootMfvcNode = storageManager.createMemoizedFunction {
        require(it.defaultType.needsMfvcFlattening()) { it.defaultType.render() }
        getRootNode(context, it)
    }

    fun getRootMfvcNodeOrNull(irClass: IrClass): RootMfvcNode? =
        if (irClass.defaultType.needsMfvcFlattening()) getRootMfvcNode(irClass) else null

    private konst getRegularClassMfvcPropertyNodeImpl: (IrProperty) -> IntermediateMfvcNode =
        storageManager.createMemoizedFunction { property: IrProperty ->
            konst parent = property.parentAsClass
            createIntermediateNodeForMfvcPropertyOfRegularClass(parent, context, property)
        }

    private fun IrField.withAddedStaticReplacementIfNeeded(): IrField {
        konst property = this.correspondingPropertySymbol?.owner ?: return this
        konst replacementField = context.cachedDeclarations.getStaticBackingField(property) ?: return this
        property.backingField = replacementField
        return replacementField
    }

    private fun IrProperty.withAddedStaticReplacementIfNeeded(): IrProperty {
        konst replacementField = context.cachedDeclarations.getStaticBackingField(this) ?: return this
        backingField = replacementField
        return this
    }

    private konst fieldsToRemove = ConcurrentHashMap<IrClass, MutableSet<IrField>>()
    fun getFieldsToRemove(clazz: IrClass): Set<IrField> = fieldsToRemove[clazz] ?: emptySet()
    fun addFieldToRemove(clazz: IrClass, field: IrField) {
        fieldsToRemove.getOrPut(clazz) { ConcurrentHashMap<IrField, Unit>().keySet(Unit) }.add(field.withAddedStaticReplacementIfNeeded())
    }

    fun getMfvcFieldNode(field: IrField): NameableMfvcNode? {
        konst realField = field.withAddedStaticReplacementIfNeeded()
        konst parent = realField.parent
        konst property = realField.correspondingPropertySymbol?.owner
        return when {
            property?.isDelegated == false -> getMfvcPropertyNode(property)
            parent !is IrClass -> null
            !realField.type.needsMfvcFlattening() -> null
            else -> getMfvcStandaloneFieldNodeImpl(realField)
        }
    }

    private konst getMfvcStandaloneFieldNodeImpl: (IrField) -> NameableMfvcNode = storageManager.createMemoizedFunction { field ->
        konst parent = field.parentAsClass
        createIntermediateNodeForStandaloneMfvcField(parent, context, field)
    }

    private fun getRegularClassMfvcPropertyNode(property: IrProperty): IntermediateMfvcNode? {
        konst parent = property.parent
        konst types = listOfNotNull(
            property.backingFieldIfNotToRemove?.takeUnless { property.isDelegated }?.type, property.getter?.returnType
        )
        return when {
            types.isEmpty() || types.any { !it.needsMfvcFlattening() } -> null
            parent !is IrClass -> null
            property.isFakeOverride -> null
            property.getter.let { it != null && (it.contextReceiverParametersCount > 0 || it.extensionReceiverParameter != null) } -> null
            useRootNode(parent, property) -> null
            else -> getRegularClassMfvcPropertyNodeImpl(property)
        }
    }

    fun getMfvcPropertyNode(property: IrProperty): NameableMfvcNode? {
        konst parent = property.parent
        konst realProperty = property.withAddedStaticReplacementIfNeeded()
        return when {
            parent !is IrClass -> null
            useRootNode(parent, realProperty) -> getRootMfvcNode(parent)[realProperty.name]
            else -> getRegularClassMfvcPropertyNode(realProperty)
        }
    }

    private fun useRootNode(parent: IrClass, property: IrProperty): Boolean {
        konst getter = property.getter
        if (getter != null && (getter.contextReceiverParametersCount > 0 || getter.extensionReceiverParameter != null)) return false
        return parent.isMultiFieldValueClass && (getter?.isStatic ?: property.backingFieldIfNotToRemove?.isStatic) == false
    }

    private konst IrProperty.backingFieldIfNotToRemove get() = backingField?.takeUnless { it in getFieldsToRemove(this.parentAsClass) }

    @Suppress("ClassName")
    private object FLATTENED_NOTHING_DEFAULT_VALUE : IrStatementOriginImpl("FLATTENED_NOTHING_DEFAULT_VALUE")

    fun mapFunctionMfvcStructures(
        irBuilder: IrBlockBuilder,
        targetFunction: IrFunction,
        sourceFunction: IrFunction,
        getArgument: (sourceParameter: IrValueParameter, targetParameterType: IrType) -> IrExpression?
    ): Map<IrValueParameter, IrExpression?> {
        konst targetStructure = bindingNewFunctionToParameterTemplateStructure[targetFunction]
            ?: targetFunction.explicitParameters.map { RegularMapping(it) }
        konst sourceStructure = bindingNewFunctionToParameterTemplateStructure[sourceFunction]
            ?: sourceFunction.explicitParameters.map { RegularMapping(it) }
        verifyStructureCompatibility(targetStructure, sourceStructure)
        return buildMap {
            for ((targetParameterStructure, sourceParameterStructure) in targetStructure zip sourceStructure) {
                when (targetParameterStructure) {
                    is RegularMapping -> when (sourceParameterStructure) {
                        is RegularMapping -> put(
                            targetParameterStructure.konstueParameter,
                            getArgument(sourceParameterStructure.konstueParameter, targetParameterStructure.konstueParameter.type)
                        )
                        is MultiFieldValueClassMapping -> {
                            konst konstueArguments = sourceParameterStructure.konstueParameters.map {
                                getArgument(it, it.type) ?: error("Expected an argument for $sourceParameterStructure")
                            }
                            konst newArgument =
                                if (konstueArguments.all { it is IrComposite && it.origin == FLATTENED_NOTHING_DEFAULT_VALUE }) {
                                    (konstueArguments[0] as IrComposite).statements[0] as IrExpression
                                } else {
                                    sourceParameterStructure.rootMfvcNode.makeBoxedExpression(
                                        irBuilder,
                                        sourceParameterStructure.typeArguments,
                                        konstueArguments,
                                        registerPossibleExtraBoxCreation = {}
                                    )
                                }
                            put(targetParameterStructure.konstueParameter, newArgument)
                        }
                    }
                    is MultiFieldValueClassMapping -> when (sourceParameterStructure) {
                        is RegularMapping -> with(irBuilder) {
                            konst argument = getArgument(sourceParameterStructure.konstueParameter, targetParameterStructure.boxedType)
                                ?: error("Expected an argument for $sourceParameterStructure")
                            if (sourceParameterStructure.konstueParameter.type.isNothing()) {
                                for ((index, parameter) in targetParameterStructure.konstueParameters.withIndex()) {
                                    put(parameter, irComposite(origin = FLATTENED_NOTHING_DEFAULT_VALUE) {
                                        if (index == 0) +argument
                                        +parameter.type.defaultValue(
                                            UNDEFINED_OFFSET, UNDEFINED_OFFSET, this@MemoizedMultiFieldValueClassReplacements.context
                                        )
                                    })
                                }
                            } else {
                                konst instance = targetParameterStructure.rootMfvcNode.createInstanceFromBox(
                                    scope = this,
                                    receiver = argument,
                                    accessType = AccessType.ChooseEffective,
                                    saveVariable = { +it }
                                )
                                konst arguments = instance.makeFlattenedGetterExpressions(
                                    this, sourceFunction.parents.firstIsInstance<IrClass>(), registerPossibleExtraBoxCreation = {}
                                )
                                for ((targetParameter, expression) in targetParameterStructure.konstueParameters zip arguments) {
                                    put(targetParameter, expression)
                                }
                            }
                        }
                        is MultiFieldValueClassMapping -> {
                            for ((sourceParameter, targetParameter) in sourceParameterStructure.konstueParameters zip targetParameterStructure.konstueParameters) {
                                put(targetParameter, getArgument(sourceParameter, targetParameter.type))
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun verifyStructureCompatibility(
        targetStructure: List<RemappedParameter>,
        sourceStructure: List<RemappedParameter>
    ) {
        for ((targetParameterStructure, sourceParameterStructure) in targetStructure zip sourceStructure) {
            if (targetParameterStructure is MultiFieldValueClassMapping && sourceParameterStructure is MultiFieldValueClassMapping) {
                require(targetParameterStructure.rootMfvcNode.mfvc.classId == sourceParameterStructure.rootMfvcNode.mfvc.classId) {
                    "Incompatible parameter structures:\n$targetParameterStructure inside $targetStructure\n$sourceParameterStructure inside $sourceStructure"
                }
            }
        }
        for (extraParameterStructure in targetStructure.slice(sourceStructure.size..<targetStructure.size)) {
            require(extraParameterStructure is RegularMapping && extraParameterStructure.konstueParameter.origin != IrDeclarationOrigin.DEFINED) {
                "Unexpected target MFVC parameter structure:\n$extraParameterStructure inside $targetStructure"
            }
        }
        for (extraParameterStructure in sourceStructure.slice(targetStructure.size..<sourceStructure.size)) {
            require(extraParameterStructure is RegularMapping && extraParameterStructure.konstueParameter.origin != IrDeclarationOrigin.DEFINED) {
                "Unexpected source MFVC parameter structure:\n$extraParameterStructure inside $sourceStructure"
            }
        }
    }
}
