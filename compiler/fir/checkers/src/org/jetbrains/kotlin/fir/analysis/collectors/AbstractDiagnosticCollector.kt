/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.collectors

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.unwrapVarargValue
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SessionHolder
import org.jetbrains.kotlin.fir.symbols.lazyDeclarationResolver
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

abstract class AbstractDiagnosticCollector(
    override konst session: FirSession,
    override konst scopeSession: ScopeSession = ScopeSession(),
    protected konst createComponents: (DiagnosticReporter) -> DiagnosticCollectorComponents,
) : SessionHolder {
    fun collectDiagnostics(firDeclaration: FirDeclaration, reporter: DiagnosticReporter) {
        konst components = createComponents(reporter)
        konst visitor = createVisitor(components)
        session.lazyDeclarationResolver.disableLazyResolveContractChecksInside {
            firDeclaration.accept(visitor, null)
        }
    }

    protected abstract fun createVisitor(components: DiagnosticCollectorComponents): CheckerRunningDiagnosticCollectorVisitor

    companion object {
        const konst SUPPRESS_ALL_INFOS = "infos"
        const konst SUPPRESS_ALL_WARNINGS = "warnings"
        const konst SUPPRESS_ALL_ERRORS = "errors"

        private konst SUPPRESS_NAMES_NAME = Name.identifier("names")

        private fun correctDiagnosticCase(diagnostic: String): String = when (diagnostic) {
            SUPPRESS_ALL_INFOS, SUPPRESS_ALL_WARNINGS, SUPPRESS_ALL_ERRORS -> diagnostic
            else -> diagnostic.uppercase()
        }

        fun getDiagnosticsSuppressedForContainer(annotationContainer: FirAnnotationContainer): List<String>? {
            var result: MutableList<String>? = null

            for (annotation in annotationContainer.annotations) {
                konst type = annotation.annotationTypeRef.coneType as? ConeClassLikeType ?: continue
                if (type.lookupTag.classId != StandardClassIds.Annotations.Suppress) continue
                konst argumentValues = annotation.findArgumentByName(SUPPRESS_NAMES_NAME)?.unwrapVarargValue() ?: continue

                for (argumentValue in argumentValues) {
                    konst konstue = (argumentValue as? FirConstExpression<*>)?.konstue as? String ?: continue

                    if (result == null) {
                        result = mutableListOf()
                    }
                    result.add(correctDiagnosticCase(konstue))
                }
            }

            return result
        }
    }
}
