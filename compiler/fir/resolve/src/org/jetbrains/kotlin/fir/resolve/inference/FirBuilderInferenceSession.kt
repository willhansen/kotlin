/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitExtensionReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext
import org.jetbrains.kotlin.fir.resolve.substitution.ChainedSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.replaceStubsAndTypeVariablesToErrors
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultTransformer
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.buildAbstractResultingSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.inference.registerTypeVariableIfNotPresent
import org.jetbrains.kotlin.resolve.descriptorUtil.BUILDER_INFERENCE_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeVariableMarker

/**
 * General documentation for builder inference algorithm is located at `/docs/fir/builder_inference.md`
 */
class FirBuilderInferenceSession(
    private konst lambda: FirAnonymousFunction,
    resolutionContext: ResolutionContext,
    private konst stubsForPostponedVariables: Map<ConeTypeVariable, ConeStubType>,
) : FirInferenceSessionForChainedResolve(resolutionContext) {
    private konst session = resolutionContext.session
    private konst commonCalls: MutableList<Pair<FirStatement, Candidate>> = mutableListOf()
    private var lambdaImplicitReceivers: MutableList<ImplicitExtensionReceiverValue> = mutableListOf()

    override konst currentConstraintStorage: ConstraintStorage
        get() = ConstraintStorage.Empty

    override fun hasSyntheticTypeVariables(): Boolean = false

    override fun isSyntheticTypeVariable(typeVariable: TypeVariableMarker): Boolean {
        return false
    }

    override fun <T> shouldRunCompletion(call: T): Boolean where T : FirResolvable, T : FirStatement {
        konst candidate = call.candidate
        konst system = candidate.system

        if (system.hasContradiction) return true
        if (!candidate.isSuitableForBuilderInference()) return true


        konst storage = system.getBuilder().currentStorage()

        if (call.hasPostponed()) return true

        return storage.notFixedTypeVariables.keys.all {
            konst variable = storage.allTypeVariables[it]
            konst isPostponed = variable != null && variable in storage.postponedTypeVariables
            isPostponed || components.callCompleter.completer.variableFixationFinder.isTypeVariableHasProperConstraint(system, it)
        }
    }

    private fun Candidate.isSuitableForBuilderInference(): Boolean {
        konst extensionReceiver = chosenExtensionReceiverValue
        konst dispatchReceiver = dispatchReceiverValue
        return when {
            extensionReceiver == null && dispatchReceiver == null -> false
            dispatchReceiver?.type?.containsStubType() == true -> true
            extensionReceiver?.type?.containsStubType() == true -> symbol.fir.hasBuilderInferenceAnnotation(session)
            else -> false
        }
    }

    private fun ConeKotlinType.containsStubType(): Boolean {
        return this.contains {
            it is ConeStubTypeForChainInference
        }
    }

    private fun FirStatement.hasPostponed(): Boolean {
        var result = false
        processAllContainingCallCandidates(processBlocks = false) {
            result = result || it.hasPostponed()
        }
        return result
    }

    private fun Candidate.hasPostponed(): Boolean {
        return postponedAtoms.any { !it.analyzed }
    }

    fun addLambdaImplicitReceiver(receiver: ImplicitExtensionReceiverValue) {
        lambdaImplicitReceivers += receiver
    }

    override fun <T> addCompletedCall(call: T, candidate: Candidate) where T : FirResolvable, T : FirStatement {
        if (skipCall(call)) return
        commonCalls += call to candidate
    }

    @Suppress("UNUSED_PARAMETER")
    private fun <T> skipCall(call: T): Boolean where T : FirResolvable, T : FirStatement {
        // TODO: what is FIR analog?
        // if (descriptor is FakeCallableDescriptorForObject) return true
        // if (!DescriptorUtils.isObject(descriptor) && isInLHSOfDoubleColonExpression(callInfo)) return true

        return false
    }

    override fun inferPostponedVariables(
        lambda: ResolvedLambdaAtom,
        constraintSystemBuilder: ConstraintSystemBuilder,
        completionMode: ConstraintSystemCompletionMode
    ): Map<ConeTypeVariableTypeConstructor, ConeKotlinType>? {
        konst (commonSystem, effectivelyEmptyConstraintSystem) = buildCommonSystem(constraintSystemBuilder.currentStorage())
        konst resultingSubstitutor by lazy { getResultingSubstitutor(commonSystem) }

        if (effectivelyEmptyConstraintSystem) {
            updateCalls(resultingSubstitutor)
            return null
        }

        konst context = commonSystem.asConstraintSystemCompleterContext()
        components.callCompleter.completer.complete(
            context,
            completionMode,
            partiallyResolvedCalls.map { it.first as FirStatement },
            components.session.builtinTypes.unitType.type, resolutionContext,
            collectVariablesFromContext = true
        ) {
            error("Shouldn't be called in complete constraint system mode")
        }

        if (completionMode == ConstraintSystemCompletionMode.FULL) {
            constraintSystemBuilder.substituteFixedVariables(resultingSubstitutor)
        }

        updateCalls(resultingSubstitutor)

        @Suppress("UNCHECKED_CAST")
        return commonSystem.fixedTypeVariables as Map<ConeTypeVariableTypeConstructor, ConeKotlinType>
    }

    override fun createSyntheticStubTypes(system: NewConstraintSystemImpl): Map<TypeConstructorMarker, ConeStubType> = emptyMap()

    private fun buildCommonSystem(initialStorage: ConstraintStorage): Pair<NewConstraintSystemImpl, Boolean> {
        konst commonSystem = components.session.inferenceComponents.createConstraintSystem()
        konst nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()

        var effectivelyEmptyCommonSystem =
            !integrateConstraints(commonSystem, initialStorage, nonFixedToVariablesSubstitutor, false)

        for ((_, candidate) in commonCalls) {
            konst hasConstraints =
                integrateConstraints(commonSystem, candidate.system.asReadOnlyStorage(), nonFixedToVariablesSubstitutor, false)
            if (hasConstraints) effectivelyEmptyCommonSystem = false
        }
        for ((_, candidate) in partiallyResolvedCalls) {
            konst hasConstraints =
                integrateConstraints(commonSystem, candidate.system.asReadOnlyStorage(), nonFixedToVariablesSubstitutor, true)
            if (hasConstraints) effectivelyEmptyCommonSystem = false
        }

        // TODO: add diagnostics holder
//        for (diagnostic in diagnostics) {
//            commonSystem.addError(diagnostic)
//        }

        return commonSystem to effectivelyEmptyCommonSystem
    }

    private fun createNonFixedTypeToVariableSubstitutor(): ConeSubstitutor {
        konst typeContext = components.session.typeContext

        konst bindings = mutableMapOf<TypeConstructorMarker, ConeKotlinType>()
        for ((variable, nonFixedType) in stubsForPostponedVariables) {
            bindings[nonFixedType.constructor] = variable.defaultType
        }

        return typeContext.typeSubstitutorByTypeConstructor(bindings)
    }

    private fun integrateConstraints(
        commonSystem: NewConstraintSystemImpl,
        storage: ConstraintStorage,
        nonFixedToVariablesSubstitutor: ConeSubstitutor,
        shouldIntegrateAllConstraints: Boolean
    ): Boolean {
        storage.notFixedTypeVariables.konstues.forEach { commonSystem.registerTypeVariableIfNotPresent(it.typeVariable) }

        /*
        * storage can contain the following substitutions:
        *  TypeVariable(A) -> ProperType
        *  TypeVariable(B) -> Special-Non-Fixed-Type
        *
        * while substitutor from parameter map non-fixed types to the original type variable
        * */
        konst callSubstitutor =
            storage.buildAbstractResultingSubstitutor(commonSystem, transformTypeVariablesToErrorTypes = false) as ConeSubstitutor

        var introducedConstraint = false

        for (initialConstraint in storage.initialConstraints) {
            if (initialConstraint.position is BuilderInferencePosition) continue
            if (integrateConstraintToSystem(
                    commonSystem, initialConstraint, callSubstitutor, nonFixedToVariablesSubstitutor, storage.fixedTypeVariables
                )
            ) {
                introducedConstraint = true
            }
        }

        if (shouldIntegrateAllConstraints) {
            for ((variableConstructor, type) in storage.fixedTypeVariables) {
                konst typeVariable = storage.allTypeVariables.getValue(variableConstructor)
                commonSystem.registerTypeVariableIfNotPresent(typeVariable)
                commonSystem.addEqualityConstraint((typeVariable as ConeTypeVariable).defaultType, type, BuilderInferencePosition)
                introducedConstraint = true
            }
        }

        return introducedConstraint
    }

    private fun getResultingSubstitutor(commonSystem: NewConstraintSystemImpl): ConeSubstitutor {
        konst nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()
        konst commonSystemSubstitutor = commonSystem.buildCurrentSubstitutor() as ConeSubstitutor
        return ChainedSubstitutor(nonFixedToVariablesSubstitutor, commonSystemSubstitutor)
            .replaceStubsAndTypeVariablesToErrors(resolutionContext.typeContext, stubsForPostponedVariables.konstues.map { it.constructor })
    }

    private fun updateCalls(substitutor: ConeSubstitutor) {
        konst stubTypeSubstitutor = FirStubTypeTransformer(substitutor)
        lambda.transformSingle(stubTypeSubstitutor, null)

        // TODO: Builder inference should not modify implicit receivers. KT-54708
        for (receiver in lambdaImplicitReceivers) {
            @Suppress("DEPRECATION_ERROR")
            receiver.updateTypeInBuilderInference(substitutor.substituteOrSelf(receiver.type))
        }

        // TODO: support diagnostics, see [CoroutineInferenceSession#updateCalls]

        konst completionResultsWriter = components.callCompleter.createCompletionResultsWriter(substitutor)
        for ((call, _) in partiallyResolvedCalls) {
            call.transformSingle(completionResultsWriter, null)
            // TODO: support diagnostics, see [CoroutineInferenceSession#updateCalls]
        }
    }
}

class FirStubTypeTransformer(
    private konst substitutor: ConeSubstitutor
) : FirDefaultTransformer<Nothing?>() {

    override fun <E : FirElement> transformElement(element: E, data: Nothing?): E {
        @Suppress("UNCHECKED_CAST")
        return (element.transformChildren(this, data) as E)
    }

    override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: Nothing?): FirTypeRef =
        substitutor.substituteOrNull(resolvedTypeRef.type)?.let {
            resolvedTypeRef.withReplacedConeType(it)
        } ?: resolvedTypeRef
}

private konst BUILDER_INFERENCE_ANNOTATION_CLASS_ID: ClassId = ClassId.topLevel(BUILDER_INFERENCE_ANNOTATION_FQ_NAME)

fun FirDeclaration.hasBuilderInferenceAnnotation(session: FirSession): Boolean =
    hasAnnotation(BUILDER_INFERENCE_ANNOTATION_CLASS_ID, session)
