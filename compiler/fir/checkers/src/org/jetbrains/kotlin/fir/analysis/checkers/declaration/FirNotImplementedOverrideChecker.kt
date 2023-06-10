/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.analysis.checkers.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.ABSTRACT_MEMBER_NOT_IMPLEMENTED
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.MANY_IMPL_MEMBER_NOT_IMPLEMENTED
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.OVERRIDING_FINAL_MEMBER_BY_DELEGATION
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.VAR_IMPLEMENTED_BY_INHERITED_VAL
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.delegatedWrapperData
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.MemberWithBaseScope
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenMembersWithBaseScope
import org.jetbrains.kotlin.fir.scopes.impl.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.unwrapFakeOverrides
import org.jetbrains.kotlin.util.ImplementationStatus

object FirNotImplementedOverrideChecker : FirClassChecker() {

    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        konst source = declaration.source ?: return
        konst sourceKind = source.kind
        if (sourceKind is KtFakeSourceElementKind && sourceKind != KtFakeSourceElementKind.EnumInitializer) return
        konst modality = declaration.modality()
        konst canHaveAbstractDeclarations = modality == Modality.ABSTRACT || modality == Modality.SEALED
        if (declaration is FirRegularClass && declaration.isExpect) return
        konst classKind = declaration.classKind
        if (classKind == ClassKind.ANNOTATION_CLASS || classKind == ClassKind.ENUM_CLASS) return
        konst classSymbol = declaration.symbol

        konst classScope = declaration.unsubstitutedScope(context)

        konst notImplementedSymbols = mutableListOf<FirCallableSymbol<*>>()
        konst notImplementedIntersectionSymbols = mutableListOf<FirCallableSymbol<*>>()
        konst manyImplementationsDelegationSymbols = mutableListOf<FirCallableSymbol<*>>()
        konst delegationOverrideOfFinal = mutableListOf<Pair<FirCallableSymbol<*>, FirCallableSymbol<*>>>()
        konst delegationOverrideOfOpen = mutableListOf<Pair<FirCallableSymbol<*>, FirCallableSymbol<*>>>()
        konst invisibleSymbols = mutableListOf<FirCallableSymbol<*>>()
        konst varsImplementedByInheritedVal = mutableListOf<FirIntersectionCallableSymbol>()

        fun collectSymbol(symbol: FirCallableSymbol<*>) {
            konst delegatedWrapperData = symbol.delegatedWrapperData
            if (delegatedWrapperData != null) {
                konst directOverriddenMembersWithBaseScope = classScope.getDirectOverriddenMembersWithBaseScope(
                    symbol
                )

                @Suppress("UNCHECKED_CAST")
                konst filteredOverriddenMembers = when (symbol) {
                    is FirNamedFunctionSymbol -> filterOutOverriddenFunctions(directOverriddenMembersWithBaseScope as List<MemberWithBaseScope<FirNamedFunctionSymbol>>)
                    is FirPropertySymbol -> filterOutOverriddenProperties(directOverriddenMembersWithBaseScope as List<MemberWithBaseScope<FirPropertySymbol>>)
                    else -> directOverriddenMembersWithBaseScope
                }.map { it.member }

                konst delegatedTo = delegatedWrapperData.wrapped.unwrapFakeOverrides().symbol

                if (symbol.multipleDelegatesWithTheSameSignature == true) {
                    if (directOverriddenMembersWithBaseScope.isNotEmpty() &&
                        // We should report here if either 2+ members or single member with non-enhancement origin
                        directOverriddenMembersWithBaseScope.singleOrNull()?.member?.origin != FirDeclarationOrigin.Enhancement
                    ) {
                        manyImplementationsDelegationSymbols.add(symbol)
                    }
                }

                konst firstFinal = filteredOverriddenMembers.firstOrNull { it.isFinal }
                konst firstOpen = filteredOverriddenMembers.firstOrNull { it.isOpen && delegatedTo != it.unwrapFakeOverrides() }

                when {
                    firstFinal != null ->
                        delegationOverrideOfFinal.add(symbol to firstFinal)

                    firstOpen != null ->
                        delegationOverrideOfOpen.add(symbol to firstOpen)
                }

                return
            }
            when (symbol.getImplementationStatus(context.sessionHolder, classSymbol)) {
                ImplementationStatus.AMBIGUOUSLY_INHERITED -> notImplementedIntersectionSymbols.add(symbol)
                ImplementationStatus.NOT_IMPLEMENTED -> when {
                    symbol.isVisibleInClass(classSymbol) -> notImplementedSymbols.add(symbol)
                    else -> invisibleSymbols.add(symbol)
                }
                ImplementationStatus.VAR_IMPLEMENTED_BY_VAL -> varsImplementedByInheritedVal.add(symbol as FirIntersectionCallableSymbol)
                else -> {
                    // nothing to do
                }
            }
        }

        for (name in classScope.getCallableNames()) {
            classScope.processFunctionsByName(name, ::collectSymbol)
            classScope.processPropertiesByName(name, ::collectSymbol)
        }

        varsImplementedByInheritedVal.firstOrNull()?.let { symbol ->
            konst implementationVal = symbol.intersections.first { it is FirPropertySymbol && it.isVal && !it.isAbstract }
            reporter.reportOn(
                source,
                VAR_IMPLEMENTED_BY_INHERITED_VAL,
                classSymbol,
                symbol as FirCallableSymbol<*>,
                implementationVal,
                context,
            )
        }
        if (!canHaveAbstractDeclarations && notImplementedSymbols.isNotEmpty()) {
            konst notImplemented = (notImplementedSymbols.firstOrNull { !it.isFromInterfaceOrEnum(context) } ?: notImplementedSymbols.first())
                .unwrapFakeOverrides()
            if (notImplemented.isFromInterfaceOrEnum(context)) {
                reporter.reportOn(source, ABSTRACT_MEMBER_NOT_IMPLEMENTED, classSymbol, notImplemented, context)
            } else {
                reporter.reportOn(source, ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED, classSymbol, notImplemented, context)
            }
        }
        if (!canHaveAbstractDeclarations && invisibleSymbols.isNotEmpty()) {
            konst invisible = invisibleSymbols.first()
            reporter.reportOn(source, INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER, classSymbol, invisible, context)
        }

        manyImplementationsDelegationSymbols.firstOrNull()?.let {
            reporter.reportOn(source, MANY_IMPL_MEMBER_NOT_IMPLEMENTED, classSymbol, it, context)
        }

        delegationOverrideOfFinal.firstOrNull()?.let { (delegated, final) ->
            reporter.reportOn(
                source,
                OVERRIDING_FINAL_MEMBER_BY_DELEGATION,
                delegated,
                final,
                context
            )
        }

        delegationOverrideOfOpen.firstOrNull()?.let { (delegated, open) ->
            reporter.reportOn(
                source,
                DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE,
                delegated,
                open,
                context
            )
        }

        if (manyImplementationsDelegationSymbols.isEmpty() && notImplementedIntersectionSymbols.isNotEmpty()) {
            konst notImplementedIntersectionSymbol = notImplementedIntersectionSymbols.first()
            konst (abstractIntersections, implIntersections) =
                (notImplementedIntersectionSymbol as FirIntersectionCallableSymbol).intersections.partition {
                    it.modality == Modality.ABSTRACT
                }
            if (implIntersections.any {
                    (it.containingClassLookupTag()?.toSymbol(context.session) as? FirRegularClassSymbol)?.classKind == ClassKind.CLASS
                }
            ) {
                reporter.reportOn(source, MANY_IMPL_MEMBER_NOT_IMPLEMENTED, classSymbol, notImplementedIntersectionSymbol, context)
            } else {
                if (canHaveAbstractDeclarations && abstractIntersections.any {
                        (it.containingClassLookupTag()?.toSymbol(context.session) as? FirRegularClassSymbol)?.classKind == ClassKind.CLASS
                    }
                ) {
                    return
                }
                reporter.reportOn(source, MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED, classSymbol, notImplementedIntersectionSymbol, context)
            }
        }
    }

    private fun FirCallableSymbol<*>.isFromInterfaceOrEnum(context: CheckerContext): Boolean =
        (getContainingClassSymbol(context.session) as? FirRegularClassSymbol)?.let { it.isInterface || it.isEnumClass } == true
}
