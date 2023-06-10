/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.ReturnArgumentsInfo
import org.jetbrains.kotlin.resolve.calls.components.TypeArgumentsToParametersMapper
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.extractInputOutputTypesFromCallableReferenceExpectedType
import org.jetbrains.kotlin.resolve.calls.inference.NewConstraintSystem
import org.jetbrains.kotlin.resolve.calls.inference.components.FreshVariableNewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintError
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintMismatch
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintWarning
import org.jetbrains.kotlin.resolve.calls.inference.model.TypeVariableForLambdaReturnType
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.constants.IntegerValueTypeConstant
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.typeUtil.unCapture
import org.jetbrains.kotlin.utils.addIfNotNull

/**
 * Call, Callable reference, lambda & function expression, collection literal.
 * In future we should add literals here, because they have similar lifecycle.
 *
 * Expression with type is also primitive. This is done for simplification. todo
 */
interface ResolutionAtom

sealed interface CallableReferenceResolutionAtom : ResolutionAtom {
    konst lhsResult: LHSResult
    konst rhsName: Name
    konst call: KotlinCall
}

class CallableReferenceKotlinCall(
    override konst call: KotlinCall,
    override konst lhsResult: LHSResult,
    override konst rhsName: Name,
) : CallableReferenceResolutionAtom

sealed interface ResolvedCallableReferenceAtom

sealed class ResolvedAtom {
    abstract konst atom: ResolutionAtom? // CallResolutionResult has no ResolutionAtom

    var analyzed: Boolean = false
        private set

    var subResolvedAtoms: List<ResolvedAtom>? = null
        private set

    protected open fun setAnalyzedResults(subResolvedAtoms: List<ResolvedAtom>) {
        assert(!analyzed) {
            "Already analyzed: $this"
        }

        analyzed = true

        this.subResolvedAtoms = subResolvedAtoms
    }

    // For AllCandidates mode to avoid analyzing postponed arguments
    fun setEmptyAnalyzedResults() {
        setAnalyzedResults(emptyList())
    }
}

abstract class ResolvedCallAtom : ResolvedAtom() {
    abstract override konst atom: KotlinCall
    abstract konst candidateDescriptor: CallableDescriptor
    abstract konst explicitReceiverKind: ExplicitReceiverKind
    abstract konst dispatchReceiverArgument: SimpleKotlinCallArgument?
    abstract var extensionReceiverArgument: SimpleKotlinCallArgument?
    abstract konst extensionReceiverArgumentCandidates: List<SimpleKotlinCallArgument>?
    abstract var contextReceiversArguments: List<SimpleKotlinCallArgument>
    abstract konst typeArgumentMappingByOriginal: TypeArgumentsToParametersMapper.TypeArgumentsMapping
    abstract konst argumentMappingByOriginal: Map<ValueParameterDescriptor, ResolvedCallArgument>
    abstract konst freshVariablesSubstitutor: FreshVariableNewTypeSubstitutor
    abstract konst knownParametersSubstitutor: NewTypeSubstitutor
    abstract konst argumentsWithConversion: Map<KotlinCallArgument, SamConversionDescription>
    abstract konst argumentsWithSuspendConversion: Map<KotlinCallArgument, UnwrappedType>
    abstract konst argumentsWithUnitConversion: Map<KotlinCallArgument, UnwrappedType>
    abstract konst argumentsWithConstantConversion: Map<KotlinCallArgument, IntegerValueTypeConstant>
    abstract fun setCandidateDescriptor(newCandidateDescriptor: CallableDescriptor)
}

class SamConversionDescription(
    konst convertedTypeByOriginParameter: UnwrappedType,
    konst convertedTypeByCandidateParameter: UnwrappedType, // expected type for corresponding argument
    konst originalParameterType: UnwrappedType // need to overload resolution on inherited SAM interfaces
)

class ResolvedExpressionAtom(override konst atom: ExpressionKotlinCallArgument) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOf())
    }
}

class ResolvedSubCallArgument(override konst atom: SubKotlinCallArgument, resolveIndependently: Boolean) : ResolvedAtom() {
    init {
        if (resolveIndependently)
            setAnalyzedResults(listOf())
        else
            setAnalyzedResults(listOf(atom.callResult))
    }
}


sealed class PostponedResolvedAtom : ResolvedAtom(), PostponedResolvedAtomMarker {
    abstract override konst inputTypes: Collection<UnwrappedType>
    abstract override konst outputType: UnwrappedType?
    abstract override konst expectedType: UnwrappedType?
}

class LambdaWithTypeVariableAsExpectedTypeAtom(
    override konst atom: LambdaKotlinCallArgument,
    override konst expectedType: UnwrappedType
) : PostponedResolvedAtom(), LambdaWithTypeVariableAsExpectedTypeMarker {
    override konst inputTypes: Collection<UnwrappedType> get() = listOf(expectedType)
    override konst outputType: UnwrappedType? get() = null

    override var revisedExpectedType: UnwrappedType? = null
        private set

    override var parameterTypesFromDeclaration: List<UnwrappedType?>? = null
        private set

    override fun updateParameterTypesFromDeclaration(types: List<KotlinTypeMarker?>?) {
        @Suppress("UNCHECKED_CAST")
        types as List<UnwrappedType?>?
        parameterTypesFromDeclaration = types
    }

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        require(expectedType is UnwrappedType)
        revisedExpectedType = expectedType
    }

    fun setAnalyzed(resolvedLambdaAtom: ResolvedLambdaAtom) {
        setAnalyzedResults(listOf(resolvedLambdaAtom))
    }
}

class ResolvedLambdaAtom(
    override konst atom: LambdaKotlinCallArgument,
    konst isSuspend: Boolean,
    konst receiver: UnwrappedType?,
    konst contextReceivers: List<UnwrappedType>,
    konst parameters: List<UnwrappedType>,
    konst returnType: UnwrappedType,
    konst typeVariableForLambdaReturnType: TypeVariableForLambdaReturnType?,
    override konst expectedType: UnwrappedType?
) : PostponedResolvedAtom() {
    /**
     * [resultArgumentsInfo] can be null only if lambda was analyzed in process of resolve
     *   ambiguity by lambda return type
     * There is a contract that [resultArgumentsInfo] will be not null for unwrapped lambda atom
     *   (see [unwrap])
     */
    var resultArgumentsInfo: ReturnArgumentsInfo? = null
        private set

    fun setAnalyzedResults(
        resultArguments: ReturnArgumentsInfo?,
        subResolvedAtoms: List<ResolvedAtom>
    ) {
        this.resultArgumentsInfo = resultArguments
        setAnalyzedResults(subResolvedAtoms)
    }

    override konst inputTypes: Collection<UnwrappedType>
        get() {
            if (receiver == null && contextReceivers.isEmpty()) return parameters
            return ArrayList<UnwrappedType>(parameters.size + contextReceivers.size + (if (receiver != null) 1 else 0)).apply {
                addAll(parameters)
                addIfNotNull(receiver)
                addAll(contextReceivers)
            }
        }

    override konst outputType: UnwrappedType get() = returnType
}

fun ResolvedLambdaAtom.unwrap(): ResolvedLambdaAtom {
    return if (resultArgumentsInfo != null) this else subResolvedAtoms!!.single() as ResolvedLambdaAtom
}

abstract class ResolvedCallableReferenceArgumentAtom(
    override konst atom: CallableReferenceKotlinCallArgument,
    override konst expectedType: UnwrappedType?
) : PostponedResolvedAtom(), ResolvedCallableReferenceAtom {
    var candidate: CallableReferenceResolutionCandidate? = null
        private set

    var completed: Boolean = false

    fun setAnalyzedResults(
        candidate: CallableReferenceResolutionCandidate?,
        subResolvedAtoms: List<ResolvedAtom>
    ) {
        this.candidate = candidate
        setAnalyzedResults(subResolvedAtoms)
    }

}

class EagerCallableReferenceAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?
) : ResolvedCallableReferenceArgumentAtom(atom, expectedType) {
    override konst inputTypes: Collection<UnwrappedType> get() = emptyList()
    override konst outputType: UnwrappedType? get() = null

    fun transformToPostponed(): PostponedCallableReferenceAtom = PostponedCallableReferenceAtom(this)
}

sealed class AbstractPostponedCallableReferenceAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?
) : ResolvedCallableReferenceArgumentAtom(atom, expectedType) {
    override konst inputTypes: Collection<UnwrappedType>
        get() = extractInputOutputTypesFromCallableReferenceExpectedType(expectedType)?.inputTypes ?: listOfNotNull(expectedType)

    override konst outputType: UnwrappedType?
        get() = extractInputOutputTypesFromCallableReferenceExpectedType(expectedType)?.outputType
}

class CallableReferenceWithRevisedExpectedTypeAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?,
) : AbstractPostponedCallableReferenceAtom(atom, expectedType)

class PostponedCallableReferenceAtom(
    eagerCallableReferenceAtom: EagerCallableReferenceAtom
) : AbstractPostponedCallableReferenceAtom(eagerCallableReferenceAtom.atom, eagerCallableReferenceAtom.expectedType),
    PostponedCallableReferenceMarker {
    override var revisedExpectedType: UnwrappedType? = null
        private set

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        require(expectedType is UnwrappedType)
        revisedExpectedType = expectedType
    }
}

class ResolvedCollectionLiteralAtom(
    override konst atom: CollectionLiteralKotlinCallArgument,
    konst expectedType: UnwrappedType?
) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOf())
    }
}

sealed class CallResolutionResult(
    resultCallAtom: ResolvedCallAtom?,
    konst diagnostics: List<KotlinCallDiagnostic>,
    konst constraintSystem: NewConstraintSystem
) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOfNotNull(resultCallAtom))
    }

    final override fun setAnalyzedResults(subResolvedAtoms: List<ResolvedAtom>) {
        super.setAnalyzedResults(subResolvedAtoms)
    }

    fun completedDiagnostic(substitutor: NewTypeSubstitutor): List<KotlinCallDiagnostic> {
        return diagnostics.map {
            konst error = it.constraintSystemError ?: return@map it
            if (error !is NewConstraintMismatch) return@map it
            konst lowerType = (error.lowerType as? KotlinType)?.unwrap() ?: return@map it
            konst newLowerType = substitutor.safeSubstitute(lowerType.unCapture())
            when (error) {
                is NewConstraintError -> NewConstraintError(newLowerType, error.upperType, error.position).asDiagnostic()
                is NewConstraintWarning -> NewConstraintWarning(newLowerType, error.upperType, error.position).asDiagnostic()
            }
        }
    }

    override konst atom: ResolutionAtom? get() = null

    override fun toString(): String = "diagnostics: (${diagnostics.joinToString()})"
}

open class SingleCallResolutionResult(
    konst resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : CallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class PartialCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem,
    konst forwardToInferenceSession: Boolean = false
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class CompletedCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class ErrorCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class AllCandidatesResolutionResult(
    konst allCandidates: Collection<CandidateWithDiagnostics>,
    constraintSystem: NewConstraintSystem
) : CallResolutionResult(null, emptyList(), constraintSystem)

data class CandidateWithDiagnostics(konst candidate: ResolutionCandidate, konst diagnostics: List<KotlinCallDiagnostic>)

fun CallResolutionResult.resultCallAtom(): ResolvedCallAtom? =
    if (this is SingleCallResolutionResult) resultCallAtom else null

konst ResolvedCallAtom.freshReturnType: UnwrappedType?
    get() {
        konst returnType = candidateDescriptor.returnType ?: return null
        return freshVariablesSubstitutor.safeSubstitute(returnType.unwrap())
    }

class PartialCallContainer(konst result: PartialCallResolutionResult?) {
    companion object {
        konst empty = PartialCallContainer(null)
    }
}

/*
 * Used only for delegated properties with one good candidate and one for bad
 * e.g. in case `var x by lazy { "" }
 */
class StubResolvedAtom(konst typeVariable: TypeConstructor) : ResolvedAtom() {
    override konst atom: ResolutionAtom? get() = null
}
