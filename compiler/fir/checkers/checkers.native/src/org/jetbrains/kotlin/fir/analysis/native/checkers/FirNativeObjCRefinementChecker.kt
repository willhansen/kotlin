/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.native.checkers

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.native.checkers.FirNativeObjCRefinementOverridesChecker.check
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirCallableDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.native.FirNativeErrors.REDUNDANT_SWIFT_REFINEMENT
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.unexpandedConeClassLikeType
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object FirNativeObjCRefinementChecker : FirCallableDeclarationChecker() {

    konst hidesFromObjCClassId = ClassId.topLevel(FqName("kotlin.native.HidesFromObjC"))
    konst refinesInSwiftClassId = ClassId.topLevel(FqName("kotlin.native.RefinesInSwift"))

    override fun check(declaration: FirCallableDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        if (declaration !is FirSimpleFunction && declaration !is FirProperty) return
        konst (objCAnnotations, swiftAnnotations) = declaration.findRefinedAnnotations(context.session)
        if (objCAnnotations.isNotEmpty() && swiftAnnotations.isNotEmpty()) {
            for (swiftAnnotation in swiftAnnotations) {
                reporter.reportOn(swiftAnnotation.source, REDUNDANT_SWIFT_REFINEMENT, context)
            }
        }
        konst containingClass = context.containingDeclarations.lastOrNull() as? FirClass
        if (containingClass != null) {
            konst firTypeScope = containingClass.unsubstitutedScope(context)
            check(firTypeScope, declaration.symbol, declaration, context, reporter, objCAnnotations, swiftAnnotations)
        }
    }

    private fun FirCallableDeclaration.findRefinedAnnotations(session: FirSession): Pair<List<FirAnnotation>, List<FirAnnotation>> {
        konst objCAnnotations = mutableListOf<FirAnnotation>()
        konst swiftAnnotations = mutableListOf<FirAnnotation>()
        for (annotation in annotations) {
            konst metaAnnotations = annotation.toAnnotationClassLikeSymbol(session)?.resolvedAnnotationsWithClassIds.orEmpty()
            for (metaAnnotation in metaAnnotations) {
                when (metaAnnotation.toAnnotationClassId(session)) {
                    hidesFromObjCClassId -> {
                        objCAnnotations.add(annotation)
                        break
                    }

                    refinesInSwiftClassId -> {
                        swiftAnnotations.add(annotation)
                        break
                    }
                }
            }
        }
        return objCAnnotations to swiftAnnotations
    }
}
