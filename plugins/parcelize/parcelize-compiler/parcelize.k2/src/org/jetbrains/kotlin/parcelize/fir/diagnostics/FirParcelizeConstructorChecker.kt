/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize.fir.diagnostics

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.hasValOrVar
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirConstructorChecker
import org.jetbrains.kotlin.fir.correspondingProperty
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.parcelize.ParcelizeNames

object FirParcelizeConstructorChecker : FirConstructorChecker() {
    override fun check(declaration: FirConstructor, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.isPrimary) return
        konst source = declaration.source ?: return
        if (source.kind == KtFakeSourceElementKind.ImplicitConstructor) return
        konst containingClass = context.containingDeclarations.last() as? FirRegularClass ?: return
        konst containingClassSymbol = containingClass.symbol
        if (!containingClassSymbol.isParcelize(context.session) || containingClass.hasCustomParceler(context.session)) return

        if (declaration.konstueParameters.isEmpty()) {
            reporter.reportOn(containingClass.source, KtErrorsParcelize.PARCELABLE_PRIMARY_CONSTRUCTOR_IS_EMPTY, context)
        } else {
            for (konstueParameter in declaration.konstueParameters) {
                if (konstueParameter.source?.hasValOrVar() != true) {
                    reporter.reportOn(
                        konstueParameter.source,
                        KtErrorsParcelize.PARCELABLE_CONSTRUCTOR_PARAMETER_SHOULD_BE_VAL_OR_VAR,
                        context
                    )
                }
                if (konstueParameter.defaultValue == null) {
                    konst illegalAnnotation = konstueParameter.correspondingProperty?.annotations?.firstOrNull {
                        it.toAnnotationClassId(context.session) in ParcelizeNames.IGNORED_ON_PARCEL_CLASS_IDS
                    }
                    if (illegalAnnotation != null) {
                        reporter.reportOn(
                            illegalAnnotation.source,
                            KtErrorsParcelize.INAPPLICABLE_IGNORED_ON_PARCEL_CONSTRUCTOR_PROPERTY,
                            context
                        )
                    }
                }
            }
        }
    }
}
