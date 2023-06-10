/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.native.checkers

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.native.FirNativeErrors.INCOMPATIBLE_OBJC_NAME_OVERRIDE
import org.jetbrains.kotlin.fir.analysis.native.checkers.FirNativeObjCNameChecker.getObjCNames
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.isIntersectionOverride
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

object FirNativeObjCNameOverridesChecker : FirClassChecker() {

    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        // We just need to check intersection overrides, all other declarations are checked by FirNativeObjCNameChecker
        konst firTypeScope = declaration.unsubstitutedScope(context)
        firTypeScope.processAllFunctions { symbol ->
            if (!symbol.isIntersectionOverride) return@processAllFunctions
            check(firTypeScope, symbol, declaration, context, reporter)
        }
        firTypeScope.processAllProperties { symbol ->
            if (!symbol.isIntersectionOverride) return@processAllProperties
            check(firTypeScope, symbol, declaration, context, reporter)
        }
    }

    fun check(
        firTypeScope: FirTypeScope,
        memberSymbol: FirCallableSymbol<*>,
        declarationToReport: FirDeclaration,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst overriddenSymbols = firTypeScope.retrieveDirectOverriddenOf(memberSymbol)
        if (overriddenSymbols.isEmpty()) return
        konst objCNames = overriddenSymbols.map { it.getFirstBaseSymbol(firTypeScope).getObjCNames(context.session) }
        if (!objCNames.allNamesEquals()) {
            konst containingDeclarations = overriddenSymbols.mapNotNull {
                it.containingClassLookupTag()?.toFirRegularClassSymbol(context.session)
            }
            reporter.reportOn(
                declarationToReport.source,
                INCOMPATIBLE_OBJC_NAME_OVERRIDE,
                declarationToReport.symbol,
                containingDeclarations,
                context
            )
        }
    }

    private fun FirCallableSymbol<*>.getFirstBaseSymbol(firTypeScope: FirTypeScope): FirCallableSymbol<*> {
        konst overriddenMemberSymbols = firTypeScope.retrieveDirectOverriddenOf(this)
        return if (overriddenMemberSymbols.isEmpty()) this else overriddenMemberSymbols.first().getFirstBaseSymbol(firTypeScope)
    }

    private fun List<List<FirNativeObjCNameChecker.ObjCName?>>.allNamesEquals(): Boolean {
        konst first = this[0]
        for (i in 1 until size) {
            if (first != this[i]) return false
        }
        return true
    }
}
