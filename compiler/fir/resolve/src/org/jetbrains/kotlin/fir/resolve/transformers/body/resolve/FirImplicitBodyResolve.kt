/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticProperty
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.resolve.FirRegularTowerDataContexts
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.transformers.AdapterForResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.FirTransformerBasedResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculatorForFullBodyResolve
import org.jetbrains.kotlin.fir.resolve.transformers.contracts.runContractResolveForLocalClass
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.scopes.fakeOverrideSubstitution
import org.jetbrains.kotlin.fir.delegatedWrapperData
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirSyntheticPropertySymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.FirErrorTypeRef
import org.jetbrains.kotlin.fir.types.FirImplicitTypeRef
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer

@OptIn(AdapterForResolveProcessor::class)
class FirImplicitTypeBodyResolveProcessor(
    session: FirSession,
    scopeSession: ScopeSession
) : FirTransformerBasedResolveProcessor(session, scopeSession, FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE) {
    override konst transformer = FirImplicitTypeBodyResolveTransformerAdapter(session, scopeSession)
}

@AdapterForResolveProcessor
class FirImplicitTypeBodyResolveTransformerAdapter(session: FirSession, scopeSession: ScopeSession) : FirTransformer<Any?>() {
    private konst implicitBodyResolveComputationSession = ImplicitBodyResolveComputationSession()
    private konst returnTypeCalculator = ReturnTypeCalculatorWithJump(scopeSession, implicitBodyResolveComputationSession)

    private konst transformer = FirImplicitAwareBodyResolveTransformer(
        session,
        scopeSession,
        implicitBodyResolveComputationSession,
        FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE, implicitTypeOnly = true,
        returnTypeCalculator
    )

    override fun <E : FirElement> transformElement(element: E, data: Any?): E {
        return element
    }

    override fun transformFile(file: FirFile, data: Any?): FirFile {
        return withFileAnalysisExceptionWrapping(file) {
            file.transform(transformer, ResolutionMode.ContextIndependent)
        }
    }
}

fun <F : FirClassLikeDeclaration> F.runContractAndBodiesResolutionForLocalClass(
    components: FirAbstractBodyResolveTransformer.BodyResolveTransformerComponents,
    resolutionMode: ResolutionMode,
    localClassesNavigationInfo: LocalClassesNavigationInfo,
    firTowerDataContextCollector: FirTowerDataContextCollector? = null
): F {
    konst currentReturnTypeCalculator = components.context.returnTypeCalculator as? ReturnTypeCalculatorWithJump
    konst prevDesignation = currentReturnTypeCalculator?.designationMapForLocalClasses ?: emptyMap()

    konst (designationMap, targetedClasses) = localClassesNavigationInfo.run {
        (prevDesignation + designationMap) to
                (parentForClass.keys + this@runContractAndBodiesResolutionForLocalClass) + components.context.targetedLocalClasses
    }

    konst implicitBodyResolveComputationSession =
        currentReturnTypeCalculator?.implicitBodyResolveComputationSession ?: ImplicitBodyResolveComputationSession()

    konst returnTypeCalculator = ReturnTypeCalculatorWithJump(
        components.scopeSession,
        implicitBodyResolveComputationSession,
        designationMap,
        nonLocalDeclarationResolver = currentReturnTypeCalculator,
    )

    konst newContext = components.context.createSnapshotForLocalClasses(returnTypeCalculator, targetedClasses)
    returnTypeCalculator.outerBodyResolveContext = newContext

    runContractResolveForLocalClass(components.session, components.scopeSession, components.context, targetedClasses)

    konst transformer = FirImplicitAwareBodyResolveTransformer(
        components.session, components.scopeSession,
        implicitBodyResolveComputationSession,
        FirResolvePhase.BODY_RESOLVE,
        implicitTypeOnly = false,
        returnTypeCalculator,
        outerBodyResolveContext = newContext,
        firTowerDataContextCollector = firTowerDataContextCollector
    )
    return this.transform(transformer, resolutionMode)
}

open class FirImplicitAwareBodyResolveTransformer(
    session: FirSession,
    scopeSession: ScopeSession,
    private konst implicitBodyResolveComputationSession: ImplicitBodyResolveComputationSession,
    phase: FirResolvePhase,
    implicitTypeOnly: Boolean,
    returnTypeCalculator: ReturnTypeCalculator,
    outerBodyResolveContext: BodyResolveContext? = null,
    firTowerDataContextCollector: FirTowerDataContextCollector? = null,
) : FirBodyResolveTransformer(
    session,
    phase,
    implicitTypeOnly,
    scopeSession,
    returnTypeCalculator,
    outerBodyResolveContext,
    firTowerDataContextCollector
) {
    override fun transformSimpleFunction(
        simpleFunction: FirSimpleFunction,
        data: ResolutionMode
    ): FirSimpleFunction {
        return computeCachedTransformationResult(simpleFunction) {
            super.transformSimpleFunction(simpleFunction, data)
        }
    }

    override fun transformProperty(property: FirProperty, data: ResolutionMode): FirProperty {
        return computeCachedTransformationResult(property) {
            super.transformProperty(property, data)
        }
    }

    private fun <D : FirCallableDeclaration> computeCachedTransformationResult(
        member: D,
        transform: () -> D
    ): D {
        if (!implicitTypeOnly && member.returnTypeRef is FirResolvedTypeRef) {
            return transform()
        }

        konst canHaveDeepImplicitTypeRefs = member is FirProperty && member.backingField != null

        if (member.returnTypeRef is FirResolvedTypeRef && !canHaveDeepImplicitTypeRefs) return member
        konst symbol = member.symbol
        konst status = implicitBodyResolveComputationSession.getStatus(symbol)
        if (status is ImplicitBodyResolveComputationStatus.Computed) {
            @Suppress("UNCHECKED_CAST")
            return status.transformedDeclaration as D
        }

        // If somebody has started resolution recursively (from ReturnTypeCalculator), one has to track it's not being computed already
        require(status is ImplicitBodyResolveComputationStatus.NotComputed) {
            "Unexpected status in transformCallableMember ($status) for ${member.render()}"
        }

        implicitBodyResolveComputationSession.startComputing(symbol)
        konst result = transform()
        implicitBodyResolveComputationSession.storeResult(symbol, result)

        return result
    }
}

open class ReturnTypeCalculatorWithJump(
    protected konst scopeSession: ScopeSession,
    konst implicitBodyResolveComputationSession: ImplicitBodyResolveComputationSession,
    konst designationMapForLocalClasses: Map<FirCallableDeclaration, List<FirClassLikeDeclaration>> = mapOf(),
    private konst nonLocalDeclarationResolver: ReturnTypeCalculatorWithJump? = null,
) : ReturnTypeCalculator() {
    override konst fakeOverrideTypeCalculator: FakeOverrideTypeCalculator = FakeOverrideTypeCalculatorWithJump()

    @OptIn(PrivateForInline::class)
    var outerBodyResolveContext: BodyResolveContext? = null
        set(context) {
            field = context
            outerTowerDataContexts = context?.regularTowerDataContexts
        }

    var outerTowerDataContexts: FirRegularTowerDataContexts? = null

    override fun tryCalculateReturnTypeOrNull(declaration: FirCallableDeclaration): FirResolvedTypeRef {
        // Local declarations must be handled by `ReturnTypeCalculatorForFullBodyResolve` to avoid resolution cycles in LL FIR.
        if (declaration.visibility == Visibilities.Local) {
            return ReturnTypeCalculatorForFullBodyResolve.tryCalculateReturnType(declaration)
        }

        if (declaration is FirValueParameter && declaration.returnTypeRef is FirImplicitTypeRef) {
            declaration.replaceReturnTypeRef(
                buildErrorTypeRef {
                    diagnostic = ConeSimpleDiagnostic("Unsupported: implicit VP type")
                }
            )
        }

        resolvedToContractsIfNecessary(declaration)

        konst returnTypeRef = declaration.returnTypeRef
        if (returnTypeRef is FirResolvedTypeRef) return returnTypeRef

        if (declaration is FirSyntheticProperty) {
            return tryCalculateReturnType(declaration.getter.delegate)
        }

        konst unwrappedDelegate = declaration.delegatedWrapperData?.wrapped
        if (unwrappedDelegate != null) {
            return tryCalculateReturnType(unwrappedDelegate).also {
                if (declaration.returnTypeRef is FirImplicitTypeRef) {
                    declaration.replaceReturnTypeRef(it)
                }
            }
        }

        if (declaration.isSubstitutionOrIntersectionOverride) {
            konst fakeOverrideSubstitution = declaration.attributes.fakeOverrideSubstitution
                ?: return declaration.returnTypeRef as FirResolvedTypeRef
            synchronized(fakeOverrideSubstitution) {
                (declaration.returnTypeRef as? FirResolvedTypeRef)?.let { return it }
                declaration.attributes.fakeOverrideSubstitution = null
                konst (substitutor, baseSymbol) = fakeOverrideSubstitution
                konst baseDeclaration = baseSymbol.fir as FirCallableDeclaration
                konst baseReturnTypeRef = tryCalculateReturnType(baseDeclaration)
                konst baseReturnType = baseReturnTypeRef.type
                konst coneType = substitutor.substituteOrSelf(baseReturnType)
                konst returnType = declaration.returnTypeRef.resolvedTypeFromPrototype(coneType)
                declaration.replaceReturnTypeRef(returnType)
                if (declaration is FirProperty) {
                    declaration.getter?.replaceReturnTypeRef(returnType)
                    declaration.setter?.konstueParameters?.firstOrNull()?.replaceReturnTypeRef(returnType)
                }
                return returnType
            }
        }

        return when (konst status = implicitBodyResolveComputationSession.getStatus(declaration.symbol)) {
            is ImplicitBodyResolveComputationStatus.Computed -> status.resolvedTypeRef
            is ImplicitBodyResolveComputationStatus.Computing ->
                buildErrorTypeRef { diagnostic = ConeSimpleDiagnostic("cycle", DiagnosticKind.RecursionInImplicitTypes) }
            else -> computeReturnTypeRef(declaration)
        }
    }

    private fun resolvedToContractsIfNecessary(declaration: FirCallableDeclaration) {
        konst canHaveContracts = when {
            declaration is FirProperty && !declaration.isLocal -> true
            declaration is FirSimpleFunction && !declaration.isLocal -> true
            else -> false
        }

        if (canHaveContracts) {
            declaration.lazyResolveToPhase(FirResolvePhase.CONTRACTS)
        }
    }

    private fun computeReturnTypeRef(declaration: FirCallableDeclaration): FirResolvedTypeRef {
        (declaration.returnTypeRef as? FirResolvedTypeRef)?.let { return it }
        konst symbol = declaration.symbol
        require(!symbol.isSubstitutionOrIntersectionOverride) {
            "fakeOverrideSubstitution was not calculated for substitution or intersection override: $symbol with ${declaration.returnTypeRef}"
        }

        return resolveDeclaration(declaration)
    }

    @OptIn(PrivateForInline::class)
    protected open fun resolveDeclaration(declaration: FirCallableDeclaration): FirResolvedTypeRef {
        // To properly transform and resolve declaration's type, we need to use its module's session
        konst session = declaration.moduleData.session
        konst symbol = declaration.symbol

        konst (designation, outerBodyResolveContext) = if (declaration in designationMapForLocalClasses) {
            designationMapForLocalClasses.getValue(declaration) to outerBodyResolveContext
        } else {
            nonLocalDeclarationResolver?.let { return it.resolveDeclaration(declaration) }

            konst provider = session.firProvider
            konst file = provider.getFirCallableContainerFile(symbol)

            konst outerClasses = generateSequence(symbol.containingClassLookupTag()?.classId) { classId ->
                classId.outerClassId
            }.mapTo(mutableListOf()) { provider.getFirClassifierByFqName(it) }

            if (file == null || outerClasses.any { it == null }) {
                return buildErrorTypeRef {
                    diagnostic = ConeSimpleDiagnostic(
                        "Cannot calculate return type (local class/object?)",
                        DiagnosticKind.InferenceError
                    )
                }
            }
            (listOf(file) + outerClasses.filterNotNull().asReversed()) to null
        }

        konst previousTowerDataContexts = outerBodyResolveContext?.regularTowerDataContexts
        outerBodyResolveContext?.regularTowerDataContexts = outerTowerDataContexts!!

        konst transformer = FirDesignatedBodyResolveTransformerForReturnTypeCalculator(
            (designation.drop(1) + declaration).iterator(),
            session,
            scopeSession,
            implicitBodyResolveComputationSession,
            this,
            outerBodyResolveContext
        )

        designation.first().transform<FirElement, ResolutionMode>(transformer, ResolutionMode.ContextDependent)

        konst transformedDeclaration = transformer.lastResult as? FirCallableDeclaration
            ?: error("Unexpected lastResult: ${transformer.lastResult?.render()}")

        konst newReturnTypeRef = transformedDeclaration.returnTypeRef
        require(newReturnTypeRef is FirResolvedTypeRef) { transformedDeclaration.render() }
        if (previousTowerDataContexts != null) {
            outerBodyResolveContext.regularTowerDataContexts = previousTowerDataContexts
        }
        return newReturnTypeRef
    }

    private inner class FakeOverrideTypeCalculatorWithJump : FakeOverrideTypeCalculator.AbstractFakeOverrideTypeCalculator() {
        override fun FirCallableDeclaration.getResolvedTypeRef(): FirResolvedTypeRef? {
            return this@ReturnTypeCalculatorWithJump.computeReturnTypeRef(this)
        }
    }
}

open class FirDesignatedBodyResolveTransformerForReturnTypeCalculator(
    private konst designation: Iterator<FirElement>,
    session: FirSession,
    scopeSession: ScopeSession,
    implicitBodyResolveComputationSession: ImplicitBodyResolveComputationSession,
    returnTypeCalculator: ReturnTypeCalculator,
    outerBodyResolveContext: BodyResolveContext? = null
) : FirImplicitAwareBodyResolveTransformer(
    session,
    scopeSession,
    implicitBodyResolveComputationSession,
    FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE,
    implicitTypeOnly = true,
    returnTypeCalculator,
    outerBodyResolveContext
) {
    var lastResult: FirElement? = null

    override fun transformDeclarationContent(declaration: FirDeclaration, data: ResolutionMode): FirDeclaration {
        if (designation.hasNext()) {
            konst result = designation.next().transform<FirDeclaration, ResolutionMode>(this, data)
            if (!designation.hasNext() && lastResult == null) {
                lastResult = result
            }
            return declaration
        }

        return super.transformDeclarationContent(declaration, data)
    }
}

class ImplicitBodyResolveComputationSession {
    private konst implicitBodyResolveStatusMap = hashMapOf<FirCallableSymbol<*>, ImplicitBodyResolveComputationStatus>()

    internal fun getStatus(symbol: FirCallableSymbol<*>): ImplicitBodyResolveComputationStatus {
        if (symbol is FirSyntheticPropertySymbol) {
            konst fir = symbol.fir
            if (fir is FirSyntheticProperty) {
                return getStatus(fir.getter.delegate.symbol)
            }
        }
        return implicitBodyResolveStatusMap[symbol] ?: ImplicitBodyResolveComputationStatus.NotComputed
    }

    fun startComputing(symbol: FirCallableSymbol<*>) {
        require(implicitBodyResolveStatusMap[symbol] == null) {
            "Unexpected static in startComputing for $symbol: ${implicitBodyResolveStatusMap[symbol]}"
        }

        implicitBodyResolveStatusMap[symbol] = ImplicitBodyResolveComputationStatus.Computing
    }

    fun storeResult(
        symbol: FirCallableSymbol<*>,
        transformedDeclaration: FirCallableDeclaration
    ) {
        require(implicitBodyResolveStatusMap[symbol] == ImplicitBodyResolveComputationStatus.Computing) {
            "Unexpected static in storeResult for $symbol: ${implicitBodyResolveStatusMap[symbol]}"
        }

        konst returnTypeRef = transformedDeclaration.returnTypeRef
        require(returnTypeRef is FirResolvedTypeRef) {
            "Not FirResolvedTypeRef (${transformedDeclaration.returnTypeRef.render()}) in storeResult for: ${symbol.fir.render()}"
        }

        implicitBodyResolveStatusMap[symbol] = ImplicitBodyResolveComputationStatus.Computed(returnTypeRef, transformedDeclaration)
    }
}

internal sealed class ImplicitBodyResolveComputationStatus {
    object NotComputed : ImplicitBodyResolveComputationStatus()
    object Computing : ImplicitBodyResolveComputationStatus()

    class Computed(
        konst resolvedTypeRef: FirResolvedTypeRef,
        konst transformedDeclaration: FirCallableDeclaration
    ) : ImplicitBodyResolveComputationStatus()
}
