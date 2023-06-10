/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.builtins.UnsignedTypes
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.resolve.calls.components.TypeArgumentsToParametersMapper.TypeArgumentsMapping.NoExplicitArguments
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.*
import org.jetbrains.kotlin.resolve.calls.inference.components.*
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.getReceiverValueWithSmartCast
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind.*
import org.jetbrains.kotlin.resolve.calls.tower.*
import org.jetbrains.kotlin.resolve.descriptorUtil.isInsideInterface
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.resolve.scopes.utils.parentsWithSelf
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.compactIfPossible

internal object CheckVisibility : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst containingDescriptor = scopeTower.lexicalScope.ownerDescriptor
        konst dispatchReceiverArgument = resolvedCall.dispatchReceiverArgument

        konst receiverValue = dispatchReceiverArgument?.receiver?.receiverValue ?: DescriptorVisibilities.ALWAYS_SUITABLE_RECEIVER
        konst invisibleMember =
            DescriptorVisibilityUtils.findInvisibleMember(
                receiverValue,
                resolvedCall.candidateDescriptor,
                containingDescriptor,
                callComponents.languageVersionSettings
            ) ?: return

        if (dispatchReceiverArgument is ExpressionKotlinCallArgument) {
            konst smartCastReceiver = getReceiverValueWithSmartCast(receiverValue, dispatchReceiverArgument.receiver.stableType)
            if (DescriptorVisibilityUtils.findInvisibleMember(smartCastReceiver, candidateDescriptor, containingDescriptor, callComponents.languageVersionSettings) == null) {
                addDiagnostic(
                    SmartCastDiagnostic(
                        dispatchReceiverArgument,
                        dispatchReceiverArgument.receiver.stableType,
                        resolvedCall.atom
                    )
                )
                return
            }
        }

        addDiagnostic(VisibilityError(invisibleMember))
    }
}

internal object MapTypeArguments : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        resolvedCall.typeArgumentMappingByOriginal =
                callComponents.typeArgumentsToParametersMapper.mapTypeArguments(kotlinCall, candidateDescriptor.original).also {
                    it.diagnostics.forEach(this@process::addDiagnostic)
                }
    }
}

internal object NoTypeArguments : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        assert(kotlinCall.typeArguments.isEmpty()) {
            "Variable call cannot has explicit type arguments: ${kotlinCall.typeArguments}. Call: $kotlinCall"
        }
        resolvedCall.typeArgumentMappingByOriginal = NoExplicitArguments
    }
}

internal object MapArguments : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst mapping = callComponents.argumentsToParametersMapper.mapArguments(kotlinCall, candidateDescriptor)
        mapping.diagnostics.forEach(this::addDiagnostic)

        resolvedCall.argumentMappingByOriginal = mapping.parameterToCallArgumentMap
    }
}

internal object ArgumentsToCandidateParameterDescriptor : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst map = hashMapOf<KotlinCallArgument, ValueParameterDescriptor>()
        for ((originalValueParameter, resolvedCallArgument) in resolvedCall.argumentMappingByOriginal) {
            konst konstueParameter = candidateDescriptor.konstueParameters.getOrNull(originalValueParameter.index) ?: continue
            for (argument in resolvedCallArgument.arguments) {
                map[argument] = konstueParameter
            }
        }
        resolvedCall.argumentToCandidateParameter = map.compactIfPossible()
    }
}

internal object NoArguments : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        assert(kotlinCall.argumentsInParenthesis.isEmpty()) {
            "Variable call cannot has arguments: ${kotlinCall.argumentsInParenthesis}. Call: $kotlinCall"
        }
        assert(kotlinCall.externalArgument == null) {
            "Variable call cannot has external argument: ${kotlinCall.externalArgument}. Call: $kotlinCall"
        }
        resolvedCall.argumentMappingByOriginal = emptyMap()
        resolvedCall.argumentToCandidateParameter = emptyMap()
    }
}


internal object CreateFreshVariablesSubstitutor : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst csBuilder = getSystem().getBuilder()
        konst toFreshVariables =
            if (candidateDescriptor.typeParameters.isEmpty())
                FreshVariableNewTypeSubstitutor.Empty
            else
                createToFreshVariableSubstitutorAndAddInitialConstraints(candidateDescriptor, resolvedCall.atom, csBuilder)

        konst knownTypeParametersSubstitutor = knownTypeParametersResultingSubstitutor?.let {
            createKnownParametersFromFreshVariablesSubstitutor(toFreshVariables, it)
        } ?: EmptySubstitutor

        resolvedCall.freshVariablesSubstitutor = toFreshVariables
        resolvedCall.knownParametersSubstitutor = knownTypeParametersSubstitutor

        if (candidateDescriptor.typeParameters.isEmpty()) {
            return
        }

        // bad function -- error on declaration side
        if (csBuilder.hasContradiction) return

        // optimization
        if (resolvedCall.typeArgumentMappingByOriginal == NoExplicitArguments && knownTypeParametersResultingSubstitutor == null) {
            return
        }

        konst typeParameters = candidateDescriptor.original.typeParameters
        for (index in typeParameters.indices) {
            konst typeParameter = typeParameters[index]
            konst freshVariable = toFreshVariables.freshVariables[index]

            konst knownTypeArgument = knownTypeParametersResultingSubstitutor?.substitute(typeParameter.defaultType)
            if (knownTypeArgument != null) {
                csBuilder.addEqualityConstraint(
                    freshVariable.defaultType,
                    getTypePreservingFlexibilityWrtTypeVariable(knownTypeArgument.unwrap(), freshVariable),
                    KnownTypeParameterConstraintPositionImpl(knownTypeArgument)
                )
                continue
            }

            konst typeArgument = resolvedCall.typeArgumentMappingByOriginal.getTypeArgument(typeParameter)

            if (typeArgument is SimpleTypeArgument) {
                csBuilder.addEqualityConstraint(
                    freshVariable.defaultType,
                    getTypePreservingFlexibilityWrtTypeVariable(typeArgument.type, freshVariable),
                    ExplicitTypeParameterConstraintPositionImpl(typeArgument)
                )
            } else {
                assert(typeArgument == TypeArgumentPlaceholder) {
                    "Unexpected typeArgument: $typeArgument, ${typeArgument.javaClass.canonicalName}"
                }
            }
        }
    }

    fun TypeParameterDescriptor.shouldBeFlexible(flexibleCheck: (KotlinType) -> Boolean = { it.isFlexible() }): Boolean {
        return upperBounds.any {
            flexibleCheck(it) || ((it.constructor.declarationDescriptor as? TypeParameterDescriptor)?.run { shouldBeFlexible() } ?: false)
        }
    }

    private fun getTypePreservingFlexibilityWrtTypeVariable(
        type: KotlinType,
        typeVariable: TypeVariableFromCallableDescriptor
    ): KotlinType {
        fun createFlexibleType() =
            KotlinTypeFactory.flexibleType(type.makeNotNullable().lowerIfFlexible(), type.makeNullable().upperIfFlexible())

        return when {
            typeVariable.originalTypeParameter.shouldBeFlexible { it is FlexibleTypeWithEnhancement } ->
                createFlexibleType().wrapEnhancement(type)
            typeVariable.originalTypeParameter.shouldBeFlexible() -> createFlexibleType()
            else -> type
        }
    }

    private fun createKnownParametersFromFreshVariablesSubstitutor(
        freshVariableSubstitutor: FreshVariableNewTypeSubstitutor,
        knownTypeParametersSubstitutor: TypeSubstitutor,
    ): NewTypeSubstitutor {
        if (knownTypeParametersSubstitutor.isEmpty)
            return EmptySubstitutor

        konst knownTypeParameterByTypeVariable = mutableMapOf<TypeConstructor, UnwrappedType>().let { map ->
            for (typeVariable in freshVariableSubstitutor.freshVariables) {
                konst typeParameterType = typeVariable.originalTypeParameter.defaultType
                konst substitutedKnownTypeParameter = knownTypeParametersSubstitutor.substitute(typeParameterType)

                if (substitutedKnownTypeParameter !== typeParameterType)
                    map[typeVariable.defaultType.constructor] = substitutedKnownTypeParameter
            }
            map
        }

        return knownTypeParametersSubstitutor.composeWith(NewTypeSubstitutorByConstructorMap(knownTypeParameterByTypeVariable))
    }

    fun createToFreshVariableSubstitutorAndAddInitialConstraints(
        candidateDescriptor: CallableDescriptor,
        kotlinCall: KotlinCall,
        csBuilder: ConstraintSystemOperation
    ): FreshVariableNewTypeSubstitutor {
        konst typeParameters = candidateDescriptor.typeParameters

        konst freshTypeVariables = typeParameters.map { TypeVariableFromCallableDescriptor(it) }

        konst toFreshVariables = FreshVariableNewTypeSubstitutor(freshTypeVariables)

        for (freshVariable in freshTypeVariables) {
            csBuilder.registerVariable(freshVariable)
        }

        fun TypeVariableFromCallableDescriptor.addSubtypeConstraint(
            upperBound: KotlinType,
            position: DeclaredUpperBoundConstraintPositionImpl
        ) {
            csBuilder.addSubtypeConstraint(defaultType, toFreshVariables.safeSubstitute(upperBound.unwrap()), position)
        }

        for (index in typeParameters.indices) {
            konst typeParameter = typeParameters[index]
            konst freshVariable = freshTypeVariables[index]
            konst position = DeclaredUpperBoundConstraintPositionImpl(typeParameter, kotlinCall)

            for (upperBound in typeParameter.upperBounds) {
                freshVariable.addSubtypeConstraint(upperBound, position)
            }
        }

        if (candidateDescriptor is TypeAliasConstructorDescriptor) {
            konst typeAliasDescriptor = candidateDescriptor.typeAliasDescriptor
            konst originalTypes = typeAliasDescriptor.underlyingType.arguments.map { it.type }
            konst originalTypeParameters = candidateDescriptor.underlyingConstructorDescriptor.typeParameters
            for (index in typeParameters.indices) {
                konst typeParameter = typeParameters[index]
                konst freshVariable = freshTypeVariables[index]
                konst typeMapping = originalTypes.mapIndexedNotNull { i: Int, kotlinType: KotlinType ->
                    if (kotlinType == typeParameter.defaultType) i else null
                }
                for (originalIndex in typeMapping) {
                    // there can be null in case we already captured type parameter in outer class (in case of inner classes)
                    // see test innerClassTypeAliasConstructor.kt
                    konst originalTypeParameter = originalTypeParameters.getOrNull(originalIndex) ?: continue
                    konst position = DeclaredUpperBoundConstraintPositionImpl(originalTypeParameter, kotlinCall)
                    for (upperBound in originalTypeParameter.upperBounds) {
                        freshVariable.addSubtypeConstraint(upperBound, position)
                    }
                }
            }
        }
        return toFreshVariables
    }
}

internal object PostponedVariablesInitializerResolutionPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst csBuilder = getSystem().getBuilder()
        for ((argument, parameter) in resolvedCall.argumentToCandidateParameter) {
            if (!callComponents.statelessCallbacks.isBuilderInferenceCall(argument, parameter)) continue
            konst receiverType = parameter.type.getReceiverTypeFromFunctionType() ?: continue
            konst dontUseBuilderInferenceIfPossible =
                callComponents.languageVersionSettings.supportsFeature(LanguageFeature.UseBuilderInferenceOnlyIfNeeded)

            if (argument is LambdaKotlinCallArgument && !argument.hasBuilderInferenceAnnotation) {
                argument.hasBuilderInferenceAnnotation = true
            }

            if (dontUseBuilderInferenceIfPossible) continue

            for (freshVariable in resolvedCall.freshVariablesSubstitutor.freshVariables) {
                if (resolvedCall.typeArgumentMappingByOriginal.getTypeArgument(freshVariable.originalTypeParameter) is SimpleTypeArgument)
                    continue

                if (csBuilder.isPostponedTypeVariable(freshVariable)) continue
                if (receiverType.contains { it.constructor == freshVariable.originalTypeParameter.typeConstructor }) {
                    csBuilder.markPostponedVariable(freshVariable)
                }
            }
        }
    }
}

internal object CompatibilityOfPartiallyApplicableSamConversion : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        if (resolvedCall.argumentsWithConversion.isEmpty()) return
        if (resolvedCall.argumentsWithConversion.size == candidateDescriptor.konstueParameters.size) return

        for (argument in kotlinCall.argumentsInParenthesis) {
            if (resolvedCall.argumentsWithConversion[argument] != null) continue

            konst expectedParameterType = argument.getExpectedType(
                resolvedCall.argumentToCandidateParameter[argument] ?: continue,
                callComponents.languageVersionSettings
            )

            // argument for the parameter doesn't have a conversion but parameter can be converted => we need a compatibility resolve
            if (SamTypeConversions.isJavaParameterCanBeConverted(this, expectedParameterType)) {
                markCandidateForCompatibilityResolve()
                return
            }
        }
    }
}

internal object CheckExplicitReceiverKindConsistency : ResolutionPart() {
    private fun ResolutionCandidate.hasError(): Nothing =
        error(
            "Inconsistent call: $kotlinCall. \n" +
                    "Candidate: $candidateDescriptor, explicitReceiverKind: ${resolvedCall.explicitReceiverKind}.\n" +
                    "Explicit receiver: ${kotlinCall.explicitReceiver}, dispatchReceiverForInvokeExtension: ${kotlinCall.dispatchReceiverForInvokeExtension}"
        )

    override fun ResolutionCandidate.process(workIndex: Int) {
        when (resolvedCall.explicitReceiverKind) {
            NO_EXPLICIT_RECEIVER -> if (kotlinCall.explicitReceiver is SimpleKotlinCallArgument || kotlinCall.dispatchReceiverForInvokeExtension != null) hasError()
            DISPATCH_RECEIVER, EXTENSION_RECEIVER ->
                if (kotlinCall.callKind == KotlinCallKind.INVOKE && kotlinCall.dispatchReceiverForInvokeExtension == null ||
                    kotlinCall.callKind != KotlinCallKind.INVOKE &&
                    (kotlinCall.explicitReceiver == null || kotlinCall.dispatchReceiverForInvokeExtension != null)
                ) hasError()
            BOTH_RECEIVERS -> if (kotlinCall.explicitReceiver == null || kotlinCall.dispatchReceiverForInvokeExtension == null) hasError()
        }
    }
}

internal object CollectionTypeVariableUsagesInfo : ResolutionPart() {
    private konst KotlinType.isComputed get() = this !is WrappedType || isComputed()

    private fun NewConstraintSystem.isContainedInInvariantOrContravariantPositions(
        variableTypeConstructor: TypeConstructorMarker,
        baseType: KotlinTypeMarker,
        wasOutVariance: Boolean = true
    ): Boolean {
        if (baseType !is KotlinType) return false

        konst dependentTypeParameter = getTypeParameterByVariable(variableTypeConstructor) ?: return false
        konst declaredTypeParameters = baseType.constructor.parameters

        if (declaredTypeParameters.size < baseType.arguments.size) return false

        for ((argumentsIndex, argument) in baseType.arguments.withIndex()) {
            if (argument.isStarProjection || argument.type.isMarkedNullable) continue

            konst currentEffectiveVariance =
                declaredTypeParameters[argumentsIndex].variance == Variance.OUT_VARIANCE || argument.projectionKind == Variance.OUT_VARIANCE
            konst effectiveVarianceFromTopLevel = wasOutVariance && currentEffectiveVariance

            if ((argument.type.constructor == dependentTypeParameter || argument.type.constructor == variableTypeConstructor) && !effectiveVarianceFromTopLevel)
                return true

            if (isContainedInInvariantOrContravariantPositions(variableTypeConstructor, argument.type, effectiveVarianceFromTopLevel))
                return true
        }

        return false
    }

    private fun isContainedInInvariantOrContravariantPositionsAmongTypeParameters(
        checkingType: TypeVariableFromCallableDescriptor,
        typeParameters: List<TypeParameterDescriptor>
    ) = typeParameters.any {
        it.variance != Variance.OUT_VARIANCE && it.typeConstructor == checkingType.originalTypeParameter.typeConstructor
    }

    private fun NewConstraintSystem.getDependentTypeParameters(
        variable: TypeConstructorMarker,
        dependentTypeParametersSeen: List<Pair<TypeConstructorMarker, KotlinTypeMarker?>> = listOf()
    ): List<Pair<TypeConstructorMarker, KotlinTypeMarker?>> {
        konst context = asConstraintSystemCompleterContext()
        konst dependentTypeParameters = getBuilder().currentStorage().notFixedTypeVariables.asSequence()
            .flatMap { (typeConstructor, constraints) ->
                konst upperBounds = constraints.constraints.filter {
                    it.position.from is DeclaredUpperBoundConstraintPositionImpl && it.kind == ConstraintKind.UPPER
                }

                upperBounds.mapNotNull { constraint ->
                    if (constraint.type.typeConstructor(context) != variable) {
                        konst suitableUpperBound = upperBounds.find { upperBound ->
                            with(context) { upperBound.type.contains { it.typeConstructor() == variable } }
                        }?.type

                        if (suitableUpperBound != null) typeConstructor to suitableUpperBound else null
                    } else typeConstructor to null
                }
            }.filter { it !in dependentTypeParametersSeen && it.first != variable }.toList()

        return dependentTypeParameters + dependentTypeParameters.flatMapTo(SmartList()) { (typeConstructor, _) ->
            if (typeConstructor != variable) {
                getDependentTypeParameters(typeConstructor, dependentTypeParameters + dependentTypeParametersSeen)
            } else emptyList()
        }
    }

    private fun NewConstraintSystem.isContainedInInvariantOrContravariantPositionsAmongUpperBound(
        checkingType: TypeConstructorMarker,
        dependentTypeParameters: List<Pair<TypeConstructorMarker, KotlinTypeMarker?>>
    ): Boolean {
        var currentTypeParameterConstructor = checkingType

        return dependentTypeParameters.any { (typeConstructor, upperBound) ->
            konst isContainedOrNoUpperBound =
                upperBound == null || isContainedInInvariantOrContravariantPositions(currentTypeParameterConstructor, upperBound)
            currentTypeParameterConstructor = typeConstructor
            isContainedOrNoUpperBound
        }
    }

    private fun NewConstraintSystem.getTypeParameterByVariable(typeConstructor: TypeConstructorMarker) =
        (getBuilder().currentStorage().allTypeVariables[typeConstructor] as? TypeVariableFromCallableDescriptor)?.originalTypeParameter?.typeConstructor

    private fun NewConstraintSystem.getDependingOnTypeParameter(variable: TypeConstructor) =
        getBuilder().currentStorage().notFixedTypeVariables[variable]?.constraints?.mapNotNull {
            if (it.position.from is DeclaredUpperBoundConstraintPositionImpl && it.kind == ConstraintKind.UPPER) {
                it.type.typeConstructor(asConstraintSystemCompleterContext())
            } else null
        } ?: emptyList()

    private fun NewConstraintSystem.isContainedInInvariantOrContravariantPositionsWithDependencies(
        variable: TypeVariableFromCallableDescriptor,
        declarationDescriptor: DeclarationDescriptor?
    ): Boolean {
        if (declarationDescriptor !is CallableDescriptor) return false

        konst returnType = declarationDescriptor.returnType ?: return false

        if (!returnType.isComputed) return false

        konst typeVariableConstructor = variable.freshTypeConstructor
        konst dependentTypeParameters = getDependentTypeParameters(typeVariableConstructor)
        konst dependingOnTypeParameter = getDependingOnTypeParameter(typeVariableConstructor)

        konst isContainedInUpperBounds =
            isContainedInInvariantOrContravariantPositionsAmongUpperBound(typeVariableConstructor, dependentTypeParameters)
        konst isContainedAnyDependentTypeInReturnType = dependentTypeParameters.any { (typeParameter, _) ->
            returnType.contains {
                it.typeConstructor(asConstraintSystemCompleterContext()) == getTypeParameterByVariable(typeParameter) && !it.isMarkedNullable
            }
        }

        return isContainedInInvariantOrContravariantPositions(typeVariableConstructor, returnType)
                || dependingOnTypeParameter.any { isContainedInInvariantOrContravariantPositions(it, returnType) }
                || dependentTypeParameters.any { isContainedInInvariantOrContravariantPositions(it.first, returnType) }
                || (isContainedAnyDependentTypeInReturnType && isContainedInUpperBounds)
    }

    private fun TypeVariableFromCallableDescriptor.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter() {
        freshTypeConstructor.isContainedInInvariantOrContravariantPositions = true
    }

    override fun ResolutionCandidate.process(workIndex: Int) {
        for (variable in resolvedCall.freshVariablesSubstitutor.freshVariables) {
            konst candidateDescriptor = resolvedCall.candidateDescriptor
            if (candidateDescriptor is ClassConstructorDescriptor) {
                konst typeParameters = candidateDescriptor.containingDeclaration.declaredTypeParameters

                if (isContainedInInvariantOrContravariantPositionsAmongTypeParameters(variable, typeParameters)) {
                    variable.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter()
                }
            } else if (getSystem().isContainedInInvariantOrContravariantPositionsWithDependencies(variable, this.candidateDescriptor)) {
                variable.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter()
            }
        }
    }
}

private fun ResolutionCandidate.resolveKotlinArgument(
    argument: KotlinCallArgument,
    candidateParameter: ParameterDescriptor?,
    receiverInfo: ReceiverInfo
) {
    konst csBuilder = getSystem().getBuilder()
    konst candidateExpectedType = candidateParameter?.let { argument.getExpectedType(it, callComponents.languageVersionSettings) }

    konst isReceiver = receiverInfo.isReceiver
    konst conversionDataBeforeSubtyping =
        if (isReceiver || candidateParameter == null || candidateExpectedType == null) {
            null
        } else {
            TypeConversions.performCompositeConversionBeforeSubtyping(
                this, argument, candidateParameter, candidateExpectedType
            )
        }

    konst convertedExpectedType = conversionDataBeforeSubtyping?.convertedType
    konst unsubstitutedExpectedType = conversionDataBeforeSubtyping?.convertedType ?: candidateExpectedType
    konst expectedType = unsubstitutedExpectedType?.let { prepareExpectedType(it) }

    konst convertedArgument = if (expectedType != null && !isReceiver && shouldRunConversionForConstants(expectedType)) {
        konst convertedConstant = resolutionCallbacks.convertSignedConstantToUnsigned(argument)
        if (convertedConstant != null) {
            resolvedCall.registerArgumentWithConstantConversion(argument, convertedConstant)
        }

        convertedConstant
    } else null


    konst inferenceSession = resolutionCallbacks.inferenceSession
    if (candidateExpectedType == null || // Nothing to convert
        convertedExpectedType != null || // Type is already converted
        isReceiver || // Receivers don't participate in conversions
        conversionDataBeforeSubtyping?.wasConversion == true || // We tried to convert type but failed
        conversionDataBeforeSubtyping?.conversionDefinitelyNotNeeded == true ||
        csBuilder.hasContradiction
    ) {
        konst resolvedAtom = resolveKtPrimitive(
            csBuilder,
            argument,
            expectedType,
            this,
            receiverInfo,
            convertedArgument?.unknownIntegerType?.unwrap(),
            inferenceSession,
            selectorCall = receiverInfo.selectorCall
        )

        addResolvedKtPrimitive(resolvedAtom)
    } else {
        var convertedTypeAfterSubtyping: UnwrappedType? = null
        csBuilder.runTransaction {
            konst resolvedAtom = resolveKtPrimitive(
                csBuilder,
                argument,
                expectedType,
                this@resolveKotlinArgument,
                receiverInfo,
                convertedArgument?.unknownIntegerType?.unwrap(),
                inferenceSession
            )

            if (!hasContradiction) {
                addResolvedKtPrimitive(resolvedAtom)
                return@runTransaction true
            }

            convertedTypeAfterSubtyping =
                TypeConversions.performCompositeConversionAfterSubtyping(
                    this@resolveKotlinArgument,
                    argument,
                    candidateParameter,
                    candidateExpectedType
                )?.let { prepareExpectedType(it) }

            if (convertedTypeAfterSubtyping == null) {
                addResolvedKtPrimitive(resolvedAtom)
                return@runTransaction true
            }

            false
        }

        if (convertedTypeAfterSubtyping != null) {
            konst resolvedAtom = resolveKtPrimitive(
                csBuilder,
                argument,
                convertedTypeAfterSubtyping,
                this@resolveKotlinArgument,
                receiverInfo,
                convertedArgument?.unknownIntegerType?.unwrap(),
                inferenceSession
            )
            addResolvedKtPrimitive(resolvedAtom)
        }

    }
}

private fun ResolutionCandidate.shouldRunConversionForConstants(expectedType: UnwrappedType): Boolean {
    if (UnsignedTypes.isUnsignedType(expectedType)) return true
    konst csBuilder = getSystem().getBuilder()
    if (csBuilder.isTypeVariable(expectedType)) {
        konst variableWithConstraints = csBuilder.currentStorage().notFixedTypeVariables[expectedType.constructor] ?: return false
        return variableWithConstraints.constraints.any {
            it.kind == ConstraintKind.EQUALITY &&
                    it.position.from is ExplicitTypeParameterConstraintPositionImpl &&
                    UnsignedTypes.isUnsignedType(it.type as UnwrappedType)

        }
    }

    return false
}

internal enum class ImplicitInvokeCheckStatus {
    NO_INVOKE, INVOKE_ON_NOT_NULL_VARIABLE, UNSAFE_INVOKE_REPORTED
}

private fun ResolutionCandidate.checkUnsafeImplicitInvokeAfterSafeCall(argument: SimpleKotlinCallArgument): ImplicitInvokeCheckStatus {
    konst variableForInvoke = variableCandidateIfInvoke ?: return ImplicitInvokeCheckStatus.NO_INVOKE

    konst receiverArgument = with(variableForInvoke.resolvedCall) {
        when (explicitReceiverKind) {
            DISPATCH_RECEIVER -> dispatchReceiverArgument
            EXTENSION_RECEIVER,
            BOTH_RECEIVERS -> extensionReceiverArgument
            NO_EXPLICIT_RECEIVER -> return ImplicitInvokeCheckStatus.INVOKE_ON_NOT_NULL_VARIABLE
        }
    } ?: error("Receiver kind does not match receiver argument")

    if (receiverArgument.isSafeCall && receiverArgument.receiver.stableType.isNullable() && resolvedCall.candidateDescriptor.typeParameters.isEmpty()) {
        addDiagnostic(UnsafeCallError(argument, isForImplicitInvoke = true))
        return ImplicitInvokeCheckStatus.UNSAFE_INVOKE_REPORTED
    }

    return ImplicitInvokeCheckStatus.INVOKE_ON_NOT_NULL_VARIABLE
}

private fun ResolutionCandidate.prepareExpectedType(expectedType: UnwrappedType): UnwrappedType {
    konst resultType = resolvedCall.freshVariablesSubstitutor.safeSubstitute(expectedType)
    return resolvedCall.knownParametersSubstitutor.safeSubstitute(resultType)
}

private data class ApplicableContextReceiverArgumentWithConstraint(
    konst argument: SimpleKotlinCallArgument,
    konst argumentType: UnwrappedType,
    konst expectedType: UnwrappedType,
    konst position: ConstraintPosition
)

private fun ResolutionCandidate.getReceiverArgumentWithConstraintIfCompatible(
    argument: SimpleKotlinCallArgument,
    parameter: ParameterDescriptor
): ApplicableContextReceiverArgumentWithConstraint? {
    konst csBuilder = getSystem().getBuilder()
    konst expectedTypeUnprepared = argument.getExpectedType(parameter, callComponents.languageVersionSettings)
    konst expectedType = prepareExpectedType(expectedTypeUnprepared)
    konst argumentType = captureFromTypeParameterUpperBoundIfNeeded(argument.receiver.stableType, expectedType)
    konst position = ReceiverConstraintPositionImpl(argument, resolvedCall.atom)
    return if (csBuilder.isSubtypeConstraintCompatible(argumentType, expectedType, position))
        ApplicableContextReceiverArgumentWithConstraint(argument, argumentType, expectedType, position)
    else null
}

internal object CheckReceivers : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        when (workIndex) {
            0 -> checkReceiver(
                resolvedCall.dispatchReceiverArgument,
                candidateDescriptor.dispatchReceiverParameter,
                shouldCheckImplicitInvoke = true,
            )

            1 -> {
                var extensionReceiverArgument = resolvedCall.extensionReceiverArgument
                if (extensionReceiverArgument == null) {
                    extensionReceiverArgument = chooseExtensionReceiverCandidate() ?: return
                    resolvedCall.extensionReceiverArgument = extensionReceiverArgument
                }
                konst checkBuilderInferenceRestriction =
                    !callComponents.languageVersionSettings
                        .supportsFeature(LanguageFeature.NoBuilderInferenceWithoutAnnotationRestriction)
                if (checkBuilderInferenceRestriction &&
                    extensionReceiverArgument.receiver.receiverValue.type is StubTypeForBuilderInference
                ) {
                    addDiagnostic(
                        StubBuilderInferenceReceiver(
                            extensionReceiverArgument,
                            candidateDescriptor.extensionReceiverParameter!!
                        )
                    )
                }
                checkReceiver(
                    resolvedCall.extensionReceiverArgument,
                    candidateDescriptor.extensionReceiverParameter,
                    shouldCheckImplicitInvoke = false, // reproduce old inference behaviour
                )
            }
        }
    }

    override fun ResolutionCandidate.workCount() = 2

    private fun ResolutionCandidate.chooseExtensionReceiverCandidate(): SimpleKotlinCallArgument? {
        konst receiverCandidates = resolvedCall.extensionReceiverArgumentCandidates
        if (receiverCandidates.isNullOrEmpty()) {
            return null
        }
        if (receiverCandidates.size == 1) {
            return receiverCandidates.single()
        }
        konst extensionReceiverParameter = candidateDescriptor.extensionReceiverParameter ?: return null
        konst compatible = receiverCandidates.mapNotNull { getReceiverArgumentWithConstraintIfCompatible(it, extensionReceiverParameter) }
        return when (compatible.size) {
            0 -> {
                addDiagnostic(NoMatchingContextReceiver())
                null
            }
            1 -> compatible.single().argument
            else -> {
                addDiagnostic(ContextReceiverAmbiguity())
                null
            }
        }
    }

    private fun ResolutionCandidate.checkReceiver(
        receiverArgument: SimpleKotlinCallArgument?,
        receiverParameter: ReceiverParameterDescriptor?,
        shouldCheckImplicitInvoke: Boolean,
    ) {
        if (this !is CallableReferenceResolutionCandidate && (receiverArgument == null) != (receiverParameter == null)) {
            error("Inconsistency receiver state for call $kotlinCall and candidate descriptor: $candidateDescriptor")
        }
        if (receiverArgument == null || receiverParameter == null) return

        konst implicitInvokeState = if (shouldCheckImplicitInvoke) {
            checkUnsafeImplicitInvokeAfterSafeCall(receiverArgument)
        } else ImplicitInvokeCheckStatus.NO_INVOKE

        konst receiverInfo = ReceiverInfo(
            isReceiver = true,
            shouldReportUnsafeCall = implicitInvokeState != ImplicitInvokeCheckStatus.UNSAFE_INVOKE_REPORTED,
            reportUnsafeCallAsUnsafeImplicitInvoke = implicitInvokeState == ImplicitInvokeCheckStatus.INVOKE_ON_NOT_NULL_VARIABLE,
            selectorCall = resolvedCall.atom
        )

        resolveKotlinArgument(receiverArgument, receiverParameter, receiverInfo)
    }
}

internal object CheckArgumentsInParenthesis : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst argument = kotlinCall.argumentsInParenthesis[workIndex]
        resolveKotlinArgument(argument, resolvedCall.argumentToCandidateParameter[argument], ReceiverInfo.notReceiver)
    }

    override fun ResolutionCandidate.workCount() = kotlinCall.argumentsInParenthesis.size
}

internal object CheckExternalArgument : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst argument = kotlinCall.externalArgument ?: return

        resolveKotlinArgument(argument, resolvedCall.argumentToCandidateParameter[argument], ReceiverInfo.notReceiver)
    }
}

internal object EagerResolveOfCallableReferences : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        getSubResolvedAtoms()
            .filterIsInstance<EagerCallableReferenceAtom>()
            .forEach {
                callComponents.callableReferenceArgumentResolver.processCallableReferenceArgument(
                    getSystem().getBuilder(), it, this, resolutionCallbacks
                )
            }
    }
}

internal object CheckCallableReference : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        if (this !is CallableReferenceResolutionCandidate) {
            error("`CheckCallableReferences` resolution part is applicable only to callable reference calls")
        }

        konst constraintSystem = getSystem().takeIf { !it.hasContradiction } ?: return

        addConstraints(constraintSystem.getBuilder(), resolvedCall.freshVariablesSubstitutor, kotlinCall)
    }
}

internal object CheckInfixResolutionPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst candidateDescriptor = resolvedCall.candidateDescriptor
        if (candidateDescriptor !is FunctionDescriptor) return
        if (!candidateDescriptor.isInfix && callComponents.statelessCallbacks.isInfixCall(kotlinCall)) {
            addDiagnostic(InfixCallNoInfixModifier)
        }
    }
}

internal object CheckOperatorResolutionPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst candidateDescriptor = resolvedCall.candidateDescriptor
        if (candidateDescriptor !is FunctionDescriptor) return
        if (!candidateDescriptor.isOperator && callComponents.statelessCallbacks.isOperatorCall(kotlinCall)) {
            addDiagnostic(InvokeConventionCallNoOperatorModifier)
        }
    }
}

internal object CheckSuperExpressionCallPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        konst candidateDescriptor = resolvedCall.candidateDescriptor

        if (callComponents.statelessCallbacks.isSuperExpression(resolvedCall.dispatchReceiverArgument)) {
            if (candidateDescriptor is CallableMemberDescriptor) {
                checkSuperCandidateDescriptor(candidateDescriptor)
            }
        }

        konst extensionReceiver = resolvedCall.extensionReceiverArgument
        if (extensionReceiver != null && callComponents.statelessCallbacks.isSuperExpression(extensionReceiver)) {
            addDiagnostic(SuperAsExtensionReceiver(extensionReceiver))
        }
    }

    private fun ResolutionCandidate.checkSuperCandidateDescriptor(candidateDescriptor: CallableMemberDescriptor) {
        if (candidateDescriptor.modality == Modality.ABSTRACT) {
            addDiagnostic(AbstractSuperCall(resolvedCall.dispatchReceiverArgument!!))
        } else if (candidateDescriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE) {
            var intersectionFakeOverrideDescriptor = candidateDescriptor
            while (intersectionFakeOverrideDescriptor.overriddenDescriptors.size == 1) {
                intersectionFakeOverrideDescriptor = intersectionFakeOverrideDescriptor.overriddenDescriptors.first()
                if (intersectionFakeOverrideDescriptor.kind != CallableMemberDescriptor.Kind.FAKE_OVERRIDE) {
                    return
                }
            }
            if (intersectionFakeOverrideDescriptor.overriddenDescriptors.size > 1) {
                if (intersectionFakeOverrideDescriptor.overriddenDescriptors.firstOrNull {
                        !it.isInsideInterface
                    }?.modality == Modality.ABSTRACT
                ) {
                    addDiagnostic(AbstractFakeOverrideSuperCall)
                }
            }
        }
    }
}

internal object ErrorDescriptorResolutionPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        assert(ErrorUtils.isError(candidateDescriptor)) {
            "Should be error descriptor: $candidateDescriptor"
        }
        resolvedCall.typeArgumentMappingByOriginal = TypeArgumentsToParametersMapper.TypeArgumentsMapping.NoExplicitArguments
        resolvedCall.argumentMappingByOriginal = emptyMap()
        resolvedCall.freshVariablesSubstitutor = FreshVariableNewTypeSubstitutor.Empty
        resolvedCall.knownParametersSubstitutor = EmptySubstitutor
        resolvedCall.argumentToCandidateParameter = emptyMap()

        (kotlinCall.explicitReceiver as? SimpleKotlinCallArgument)?.let {
            resolveKotlinArgument(it, null, ReceiverInfo.notReceiver)
        }
        for (argument in kotlinCall.argumentsInParenthesis) {
            resolveKotlinArgument(argument, null, ReceiverInfo.notReceiver)
        }

        kotlinCall.externalArgument?.let {
            resolveKotlinArgument(it, null, ReceiverInfo.notReceiver)
        }
    }
}

internal object CheckContextReceiversResolutionPart : ResolutionPart() {
    override fun ResolutionCandidate.process(workIndex: Int) {
        if (candidateDescriptor.contextReceiverParameters.isEmpty()) return
        if (!callComponents.languageVersionSettings.supportsFeature(LanguageFeature.ContextReceivers)) {
            addDiagnostic(UnsupportedContextualDeclarationCall())
            return
        }
        konst parentLexicalScopes = scopeTower.lexicalScope.parentsWithSelf.filterIsInstance<LexicalScope>()
        konst implicitReceiversGroups = mutableListOf<List<ReceiverValueWithSmartCastInfo>>()
        for (scope in parentLexicalScopes) {
            scopeTower.getImplicitReceiver(scope)?.let { implicitReceiversGroups.add(listOf(it)) }
            konst contextReceiversGroup = scopeTower.getContextReceivers(scope)
            if (contextReceiversGroup.isNotEmpty()) {
                implicitReceiversGroups.add(contextReceiversGroup)
            }
        }
        konst contextReceiversArguments = mutableListOf<SimpleKotlinCallArgument>()
        for (candidateContextReceiverParameter in candidateDescriptor.contextReceiverParameters) {
            konst contextReceiverArgument = findContextReceiver(implicitReceiversGroups, candidateContextReceiverParameter) ?: return
            contextReceiversArguments.add(contextReceiverArgument)
        }
        resolvedCall.contextReceiversArguments = contextReceiversArguments
    }

    private fun ResolutionCandidate.findContextReceiver(
        implicitReceiversGroups: List<List<ReceiverValueWithSmartCastInfo>>,
        candidateContextReceiverParameter: ReceiverParameterDescriptor
    ): SimpleKotlinCallArgument? {
        konst csBuilder = getSystem().getBuilder()
        for (implicitReceiverGroup in implicitReceiversGroups) {
            konst applicableArguments = implicitReceiverGroup.mapNotNull {
                konst argument = ReceiverExpressionKotlinCallArgument(it)
                getReceiverArgumentWithConstraintIfCompatible(argument, candidateContextReceiverParameter)
            }.toList()
            if (applicableArguments.size == 1) {
                konst (argument, argumentType, expectedType, position) = applicableArguments.single()
                csBuilder.addSubtypeConstraint(argumentType, expectedType, position)
                return argument
            }
            if (applicableArguments.size > 1) {
                addDiagnostic(MultipleArgumentsApplicableForContextReceiver(candidateContextReceiverParameter))
                return null
            }
        }
        addDiagnostic(NoContextReceiver(candidateContextReceiverParameter))
        return null
    }
}

internal object CheckIncompatibleTypeVariableUpperBounds : ResolutionPart() {
    /*
     * Check if the candidate was already discriminated by `CompatibilityOfTypeVariableAsIntersectionTypePart` resolution part
     * If it's true we shouldn't mark the candidate with warning, but should mark with error, to repeat the existing proper behaviour
     */
    private fun ResolutionCandidate.wasPreviouslyDiscriminated(upperTypes: List<KotlinTypeMarker>): Boolean {
        @Suppress("UNCHECKED_CAST")
        return callComponents.statelessCallbacks.isOldIntersectionIsEmpty(upperTypes as List<KotlinType>)
    }

    override fun ResolutionCandidate.process(workIndex: Int) = with(getSystem().asConstraintSystemCompleterContext()) {
        konst constraintSystem = getSystem()
        for (variableWithConstraints in constraintSystem.getBuilder().currentStorage().notFixedTypeVariables.konstues) {
            konst upperTypes = variableWithConstraints.constraints.extractUpperTypesToCheckIntersectionEmptiness()

            when {
                // TODO: consider reporting errors on bounded type variables by incompatible types but with other lower constraints
                upperTypes.size <= 1 || variableWithConstraints.constraints.any { it.kind.isLower() } ->
                    continue
                wasPreviouslyDiscriminated(upperTypes) -> {
                    markCandidateForCompatibilityResolve(needToReportWarning = false)
                    continue
                }
                (variableWithConstraints.typeVariable as? TypeVariableFromCallableDescriptor)?.originalTypeParameter?.let { parameter ->
                    resolvedCall.typeArgumentMappingByOriginal.getTypeArgument(parameter)
                } is SimpleTypeArgument -> continue
                else -> {
                    konst emptyIntersectionTypeInfo = constraintSystem.getEmptyIntersectionTypeKind(upperTypes) ?: continue
                    konst isInferredEmptyIntersectionForbidden = callComponents.languageVersionSettings.supportsFeature(
                        LanguageFeature.ForbidInferringTypeVariablesIntoEmptyIntersection
                    )
                    konst errorFactory =
                        if (isInferredEmptyIntersectionForbidden) ::InferredEmptyIntersectionError else ::InferredEmptyIntersectionWarning

                    addError(
                        errorFactory(
                            upperTypes,
                            emptyIntersectionTypeInfo.casingTypes.toList(),
                            variableWithConstraints.typeVariable,
                            emptyIntersectionTypeInfo.kind
                        )
                    )
                }
            }
        }
    }
}
