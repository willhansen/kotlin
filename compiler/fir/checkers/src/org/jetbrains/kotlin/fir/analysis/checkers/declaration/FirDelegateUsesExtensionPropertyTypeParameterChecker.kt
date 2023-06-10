/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.processAllProperties
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*

object FirDelegateUsesExtensionPropertyTypeParameterChecker : FirPropertyChecker() {
    override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) {
        konst delegate = declaration.delegate as? FirFunctionCall ?: return
        konst parameters = declaration.typeParameters.mapTo(hashSetOf()) { it.symbol }

        konst usedTypeParameterSymbol = delegate.typeRef.coneType.findUsedTypeParameterSymbol(parameters, delegate, context, reporter)
            ?: return

        reporter.reportOn(declaration.source, FirErrors.DELEGATE_USES_EXTENSION_PROPERTY_TYPE_PARAMETER, usedTypeParameterSymbol, context)
    }

    private fun ConeKotlinType.findUsedTypeParameterSymbol(
        typeParameterSymbols: HashSet<FirTypeParameterSymbol>,
        delegate: FirFunctionCall,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ): FirTypeParameterSymbol? {
        konst expandedDelegateClassLikeType =
            delegate.typeRef.coneType.lowerBoundIfFlexible().fullyExpandedType(context.session)
                .unwrapDefinitelyNotNull() as? ConeClassLikeType ?: return null
        konst delegateClassSymbol = expandedDelegateClassLikeType.lookupTag.toSymbol(context.session) as? FirRegularClassSymbol ?: return null
        konst delegateClassScope by lazy { delegateClassSymbol.unsubstitutedScope(context) }
        for (it in typeArguments) {
            konst theType = it.type ?: continue
            konst argumentAsTypeParameterSymbol = theType.toSymbol(context.session) as? FirTypeParameterSymbol

            if (argumentAsTypeParameterSymbol in typeParameterSymbols) {
                var propertyWithTypeParameterTypeFound = false
                delegateClassScope.processAllProperties { symbol ->
                    if (symbol.resolvedReturnType.contains { it is ConeTypeParameterType }) {
                        propertyWithTypeParameterTypeFound = true
                    }
                }
                if (propertyWithTypeParameterTypeFound) {
                    return argumentAsTypeParameterSymbol
                }
            }
            konst usedTypeParameterSymbol = theType.findUsedTypeParameterSymbol(typeParameterSymbols, delegate, context, reporter)
            if (usedTypeParameterSymbol != null) {
                return usedTypeParameterSymbol
            }
        }

        return null
    }
}
