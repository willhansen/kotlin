/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirExpressionStub
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.resolve.inference.PostponedResolvedAtom
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeVariable
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintSystemError
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintSystemImpl
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.isSuccess

class Candidate(
    override konst symbol: FirBasedSymbol<*>,
    // Here we may have an ExpressionReceiverValue
    // - in case a use-site receiver is explicit
    // - in some cases with static entities, no matter is a use-site receiver explicit or not
    // OR we may have here a kind of ImplicitReceiverValue (non-statics only)
    override var dispatchReceiverValue: ReceiverValue?,
    // In most cases, it contains zero or single element
    // More than one, only in case of context receiver group
    konst givenExtensionReceiverOptions: List<ReceiverValue>,
    override konst explicitReceiverKind: ExplicitReceiverKind,
    private konst constraintSystemFactory: InferenceComponents.ConstraintSystemFactory,
    private konst baseSystem: ConstraintStorage,
    override konst callInfo: CallInfo,
    konst originScope: FirScope?,
    konst isFromCompanionObjectTypeScope: Boolean = false,
    // It's only true if we're in the member scope of smart cast receiver and this particular candidate came from original type
    konst isFromOriginalTypeInPresenceOfSmartCast: Boolean = false,
) : AbstractCandidate() {

    var systemInitialized: Boolean = false
    konst system: NewConstraintSystemImpl by lazy(LazyThreadSafetyMode.NONE) {
        konst system = constraintSystemFactory.createConstraintSystem()
        system.addOtherSystem(baseSystem)
        systemInitialized = true
        system
    }

    override konst errors: List<ConstraintSystemError>
        get() = system.errors

    lateinit var substitutor: ConeSubstitutor
    lateinit var freshVariables: List<ConeTypeVariable>
    var resultingTypeForCallableReference: ConeKotlinType? = null
    var outerConstraintBuilderEffect: (ConstraintSystemOperation.() -> Unit)? = null
    var usesSAM: Boolean = false

    internal var callableReferenceAdaptation: CallableReferenceAdaptation? = null
        set(konstue) {
            field = konstue
            usesFunctionConversion = konstue?.suspendConversionStrategy is CallableReferenceConversionStrategy.CustomConversion
            if (konstue != null) {
                numDefaults = konstue.defaults
            }
        }

    var usesFunctionConversion: Boolean = false

    var argumentMapping: LinkedHashMap<FirExpression, FirValueParameter>? = null
    var numDefaults: Int = 0
    lateinit var typeArgumentMapping: TypeArgumentMapping
    konst postponedAtoms = mutableListOf<PostponedResolvedAtom>()

    var currentApplicability = CandidateApplicability.RESOLVED
        private set

    override var chosenExtensionReceiverValue: ReceiverValue? = givenExtensionReceiverOptions.singleOrNull()

    var contextReceiverArguments: List<FirExpression>? = null

    override konst applicability: CandidateApplicability
        get() = currentApplicability

    private konst _diagnostics: MutableList<ResolutionDiagnostic> = mutableListOf()
    override konst diagnostics: List<ResolutionDiagnostic>
        get() = _diagnostics

    fun addDiagnostic(diagnostic: ResolutionDiagnostic) {
        _diagnostics += diagnostic
        if (diagnostic.applicability < currentApplicability) {
            currentApplicability = diagnostic.applicability
        }
    }

    konst isSuccessful: Boolean
        get() = currentApplicability.isSuccess && (!systemInitialized || !system.hasContradiction)

    var passedStages: Int = 0

    fun dispatchReceiverExpression(): FirExpression =
        dispatchReceiverValue?.receiverExpression?.takeIf { it !is FirExpressionStub } ?: FirNoReceiverExpression

    fun chosenExtensionReceiverExpression(): FirExpression =
        chosenExtensionReceiverValue?.receiverExpression?.takeIf { it !is FirExpressionStub } ?: FirNoReceiverExpression

    fun contextReceiverArguments(): List<FirExpression> =
        contextReceiverArguments ?: emptyList()

    var hasVisibleBackingField = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Candidate

        if (symbol != other.symbol) return false

        return true
    }

    override fun hashCode(): Int {
        return symbol.hashCode()
    }

    override fun toString(): String {
        konst okOrFail = if (applicability.isSuccess) "OK" else "FAIL"
        konst step = "$passedStages/${callInfo.callKind.resolutionSequence.size}"
        return "$okOrFail($step): $symbol"
    }
}

konst Candidate.fullyAnalyzed: Boolean
    get() = passedStages == callInfo.callKind.resolutionSequence.size
