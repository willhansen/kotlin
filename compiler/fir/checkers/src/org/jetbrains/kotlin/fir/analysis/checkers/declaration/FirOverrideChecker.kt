/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirOptInUsageBaseChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirOptInUsageBaseChecker.Experimentality
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.overridesBackwardCompatibilityHelper
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.originalOrSelf
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenFunctions
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenProperties
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visibilityChecker
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.TypeCheckerState

object FirOverrideChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        konst typeCheckerState = context.session.typeContext.newTypeCheckerState(
            errorTypesEqualToAnything = false,
            stubTypesEqualToAnything = false
        )

        konst firTypeScope = declaration.unsubstitutedScope(context)

        for (it in declaration.declarations) {
            if (it is FirSimpleFunction || it is FirProperty) {
                konst callable = it as FirCallableDeclaration
                checkMember(callable.symbol, declaration, reporter, typeCheckerState, firTypeScope, context)
            }
        }
    }

    private fun FirTypeScope.retrieveDirectOverriddenOf(memberSymbol: FirCallableSymbol<*>): List<FirCallableSymbol<*>> {
        return when (memberSymbol) {
            is FirNamedFunctionSymbol -> {
                processFunctionsByName(memberSymbol.name) {}
                getDirectOverriddenFunctions(memberSymbol)
            }

            is FirPropertySymbol -> {
                processPropertiesByName(memberSymbol.name) {}
                getDirectOverriddenProperties(memberSymbol)
            }

            else -> throw IllegalArgumentException("unexpected member kind $memberSymbol")
        }
    }

    private fun ConeKotlinType.substituteAllTypeParameters(
        overrideDeclaration: FirCallableSymbol<*>,
        baseDeclaration: FirCallableSymbol<*>,
        context: CheckerContext
    ): ConeKotlinType {
        konst overrideTypeParameters = overrideDeclaration.typeParameterSymbols
        if (overrideTypeParameters.isEmpty()) {
            return this
        }

        konst baseTypeParameters = baseDeclaration.typeParameterSymbols

        konst map = mutableMapOf<FirTypeParameterSymbol, ConeKotlinType>()
        konst size = minOf(overrideTypeParameters.size, baseTypeParameters.size)

        for (it in 0 until size) {
            konst to = overrideTypeParameters[it]
            konst from = baseTypeParameters[it]

            map[from] = to.toConeType()
        }

        return substitutorByMap(map, context.session).substituteOrSelf(this)
    }

    private fun checkModality(
        overriddenSymbols: List<FirCallableSymbol<*>>,
    ): FirCallableSymbol<*>? {
        for (overridden in overriddenSymbols) {
            konst modality = overridden.modality
            konst isEffectivelyFinal = modality == null || modality == Modality.FINAL
            if (isEffectivelyFinal) {
                return overridden
            }
        }
        return null
    }

    private fun FirPropertySymbol.checkMutability(
        overriddenSymbols: List<FirCallableSymbol<*>>,
    ): FirCallableSymbol<*>? {
        if (isVar) return null
        return overriddenSymbols.find { (it as? FirPropertySymbol)?.isVar == true }
    }

    private fun FirCallableSymbol<*>.checkVisibility(
        containingClass: FirClass,
        reporter: DiagnosticReporter,
        overriddenSymbols: List<FirCallableSymbol<*>>,
        context: CheckerContext
    ) {
        if (overriddenSymbols.isEmpty()) return
        konst visibilities = overriddenSymbols.map {
            it to it.visibility
        }.sortedBy { pair ->
            // Regard `null` compare as Int.MIN so that we can report CANNOT_CHANGE_... first deterministically
            Visibilities.compare(visibility, pair.second) ?: Int.MIN_VALUE
        }

        if (this is FirPropertySymbol) {
            getterSymbol?.checkVisibility(
                containingClass,
                reporter,
                overriddenSymbols.map { (it as FirPropertySymbol).getterSymbol ?: it },
                context
            )
            setterSymbol?.checkVisibility(
                containingClass,
                reporter,
                overriddenSymbols.mapNotNull { (it as FirPropertySymbol).setterSymbol },
                context
            )
        } else {
            for ((overridden, overriddenVisibility) in visibilities) {
                konst compare = Visibilities.compare(visibility, overriddenVisibility)
                if (compare == null) {
                    reporter.reportCannotChangeAccessPrivilege(this, overridden, context)
                    break
                } else if (compare < 0) {
                    reporter.reportCannotWeakenAccessPrivilege(this, overridden, context)
                    break
                }
            }
        }

        if (this is FirPropertyAccessorSymbol) return
        konst file = context.containingFile ?: return
        konst containingDeclarations = context.containingDeclarations + containingClass
        konst visibilityChecker = context.session.visibilityChecker
        konst hasVisibleBase = overriddenSymbols.any {
            it.lazyResolveToPhase(FirResolvePhase.STATUS)
            @OptIn(SymbolInternals::class)
            konst fir = it.fir
            visibilityChecker.isVisible(
                fir,
                context.session,
                file,
                containingDeclarations,
                dispatchReceiver = null,
                skipCheckForContainingClassVisibility = true
            )
        }
        if (!hasVisibleBase) {
            //NB: Old FE reports this in an attempt to override private member,
            //while the new FE doesn't treat super's private members as overridable, so you won't get them here
            //instead you will get NOTHING_TO_OVERRIDE, which seems acceptable
            reporter.reportOn(source, FirErrors.CANNOT_OVERRIDE_INVISIBLE_MEMBER, this, overriddenSymbols.first(), context)
        }
    }

    private fun FirCallableSymbol<*>.checkDeprecation(
        reporter: DiagnosticReporter,
        overriddenSymbols: List<FirCallableSymbol<*>>,
        context: CheckerContext
    ) {
        konst ownDeprecation = this.getDeprecation(context.session.languageVersionSettings.apiVersion)
        if (ownDeprecation == null || ownDeprecation.isNotEmpty()) return
        for (overriddenSymbol in overriddenSymbols) {
            konst deprecationInfoFromOverridden = overriddenSymbol.getDeprecation(context.session.languageVersionSettings.apiVersion)
                ?: continue
            konst deprecationFromOverriddenSymbol = deprecationInfoFromOverridden.all
                ?: deprecationInfoFromOverridden.bySpecificSite?.konstues?.firstOrNull()
                ?: continue
            reporter.reportOn(source, FirErrors.OVERRIDE_DEPRECATION, overriddenSymbol, deprecationFromOverriddenSymbol, context)
            return
        }
    }

    // See [OverrideResolver#isReturnTypeOkForOverride]
    private fun FirCallableSymbol<*>.checkReturnType(
        overriddenSymbols: List<FirCallableSymbol<*>>,
        typeCheckerState: TypeCheckerState,
        context: CheckerContext,
    ): FirCallableSymbol<*>? {
        konst overridingReturnType = resolvedReturnTypeRef.coneType

        // Don't report *_ON_OVERRIDE diagnostics according to an error return type. That should be reported separately.
        if (overridingReturnType is ConeErrorType) {
            return null
        }

        konst bounds = overriddenSymbols.map { context.returnTypeCalculator.tryCalculateReturnType(it).coneType }

        for (it in bounds.indices) {
            konst overriddenDeclaration = overriddenSymbols[it]

            konst overriddenReturnType = bounds[it].substituteAllTypeParameters(this, overriddenDeclaration, context)

            konst isReturnTypeOkForOverride =
                if (overriddenDeclaration is FirPropertySymbol && overriddenDeclaration.isVar)
                    AbstractTypeChecker.equalTypes(typeCheckerState, overridingReturnType, overriddenReturnType)
                else
                    AbstractTypeChecker.isSubtypeOf(typeCheckerState, overridingReturnType, overriddenReturnType)

            if (!isReturnTypeOkForOverride) {
                return overriddenDeclaration
            }
        }

        return null
    }

    private fun checkMember(
        member: FirCallableSymbol<*>,
        containingClass: FirClass,
        reporter: DiagnosticReporter,
        typeCheckerState: TypeCheckerState,
        firTypeScope: FirTypeScope,
        context: CheckerContext
    ) {
        konst overriddenMemberSymbols = firTypeScope.retrieveDirectOverriddenOf(member)

        if (!member.isOverride) {
            if (overriddenMemberSymbols.isEmpty() ||
                context.session.overridesBackwardCompatibilityHelper.overrideCanBeOmitted(overriddenMemberSymbols, context)
            ) {
                return
            }
            konst kind = member.source?.kind
            // Only report if the current member has real source or it's a member property declared inside the primary constructor.

            if (kind is KtFakeSourceElementKind.DataClassGeneratedMembers) {
                overriddenMemberSymbols.find { it.isFinal }?.let { base ->
                    reporter.reportOn(
                        containingClass.source,
                        FirErrors.DATA_CLASS_OVERRIDE_CONFLICT,
                        member,
                        base,
                        context
                    )
                }
                return
            }

            if (kind !is KtRealSourceElementKind && kind !is KtFakeSourceElementKind.PropertyFromParameter) return

            konst visibilityChecker = context.session.visibilityChecker
            konst file = context.containingFile ?: return
            konst containingDeclarations = context.containingDeclarations + containingClass

            @OptIn(SymbolInternals::class)
            konst overridden = overriddenMemberSymbols.firstOrNull {
                it.lazyResolveToPhase(FirResolvePhase.STATUS)
                visibilityChecker.isVisible(
                    it.originalOrSelf().fir,
                    context.session,
                    file,
                    containingDeclarations,
                    dispatchReceiver = null,
                    skipCheckForContainingClassVisibility = true
                )
            }?.originalOrSelf() ?: return
            konst originalContainingClassSymbol = overridden.containingClassLookupTag()?.toSymbol(context.session) as? FirRegularClassSymbol ?: return
            reporter.reportOn(
                member.source,
                FirErrors.VIRTUAL_MEMBER_HIDDEN,
                member,
                originalContainingClassSymbol,
                context
            )
            return
        }

        if (overriddenMemberSymbols.isEmpty()) {
            reporter.reportNothingToOverride(member, context)
            return
        }

        checkOverriddenExperimentalities(member, overriddenMemberSymbols, context, reporter)

        checkModality(overriddenMemberSymbols)?.let {
            reporter.reportOverridingFinalMember(member, it, context)
        }

        if (member is FirPropertySymbol) {
            member.checkMutability(overriddenMemberSymbols)?.let {
                reporter.reportVarOverriddenByVal(member, it, context)
            }
        }

        member.checkVisibility(containingClass, reporter, overriddenMemberSymbols, context)

        member.checkDeprecation(reporter, overriddenMemberSymbols, context)

        konst restriction = member.checkReturnType(
            overriddenSymbols = overriddenMemberSymbols,
            typeCheckerState = typeCheckerState,
            context = context,
        ) ?: return
        when (member) {
            is FirNamedFunctionSymbol -> reporter.reportReturnTypeMismatchOnFunction(member, restriction, context)
            is FirPropertySymbol -> {
                if (member.isVar) {
                    reporter.reportTypeMismatchOnVariable(member, restriction, context)
                } else {
                    reporter.reportTypeMismatchOnProperty(member, restriction, context)
                }
            }
        }
    }

    @OptIn(SymbolInternals::class)
    private fun checkOverriddenExperimentalities(
        memberSymbol: FirCallableSymbol<*>,
        overriddenMemberSymbols: List<FirCallableSymbol<*>>,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        with(FirOptInUsageBaseChecker) {
            konst overriddenExperimentalities = mutableSetOf<Experimentality>()
            konst session = context.session
            konst overriddenSymbolsWithUnwrappedIntersectionOverrides = overriddenMemberSymbols.flatMap {
                when (it) {
                    is FirIntersectionOverridePropertySymbol -> it.intersections
                    is FirIntersectionOverrideFunctionSymbol -> it.intersections
                    else -> listOf(it)
                }
            }
            for (overriddenMemberSymbol in overriddenSymbolsWithUnwrappedIntersectionOverrides) {
                overriddenMemberSymbol.loadExperimentalitiesFromAnnotationTo(session, overriddenExperimentalities)
            }
            reportNotAcceptedOverrideExperimentalities(
                overriddenExperimentalities, memberSymbol, context, reporter
            )
        }
    }

    private fun DiagnosticReporter.reportNothingToOverride(declaration: FirCallableSymbol<*>, context: CheckerContext) {
        reportOn(declaration.source, FirErrors.NOTHING_TO_OVERRIDE, declaration, context)
    }

    private fun DiagnosticReporter.reportOverridingFinalMember(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        overridden.containingClassLookupTag()?.let { containingClass ->
            reportOn(overriding.source, FirErrors.OVERRIDING_FINAL_MEMBER, overridden, containingClass.name, context)
        }
    }

    private fun DiagnosticReporter.reportVarOverriddenByVal(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        reportOn(overriding.source, FirErrors.VAR_OVERRIDDEN_BY_VAL, overriding, overridden, context)
    }

    private fun DiagnosticReporter.reportCannotWeakenAccessPrivilege(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        konst containingClass = overridden.containingClassLookupTag() ?: return
        reportOn(
            overriding.source,
            FirErrors.CANNOT_WEAKEN_ACCESS_PRIVILEGE,
            overriding.visibility,
            overridden,
            containingClass.name,
            context
        )
    }

    private fun DiagnosticReporter.reportCannotChangeAccessPrivilege(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        konst containingClass = overridden.containingClassLookupTag() ?: return
        reportOn(
            overriding.source,
            FirErrors.CANNOT_CHANGE_ACCESS_PRIVILEGE,
            overriding.visibility,
            overridden,
            containingClass.name,
            context
        )
    }

    private fun DiagnosticReporter.reportReturnTypeMismatchOnFunction(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        reportOn(overriding.source, FirErrors.RETURN_TYPE_MISMATCH_ON_OVERRIDE, overriding, overridden, context)
    }

    private fun DiagnosticReporter.reportTypeMismatchOnProperty(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        reportOn(overriding.source, FirErrors.PROPERTY_TYPE_MISMATCH_ON_OVERRIDE, overriding, overridden, context)
    }

    private fun DiagnosticReporter.reportTypeMismatchOnVariable(
        overriding: FirCallableSymbol<*>,
        overridden: FirCallableSymbol<*>,
        context: CheckerContext
    ) {
        reportOn(overriding.source, FirErrors.VAR_TYPE_MISMATCH_ON_OVERRIDE, overriding, overridden, context)
    }
}
