/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize.fir.diagnostics

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirAnnotationCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.findClosestClassOrObject
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassLikeType
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.parcelize.ParcelizeNames
import org.jetbrains.kotlin.parcelize.ParcelizeNames.DEPRECATED_RUNTIME_PACKAGE
import org.jetbrains.kotlin.parcelize.ParcelizeNames.IGNORED_ON_PARCEL_CLASS_IDS
import org.jetbrains.kotlin.parcelize.ParcelizeNames.PARCELIZE_CLASS_CLASS_IDS
import org.jetbrains.kotlin.parcelize.ParcelizeNames.RAW_VALUE_ANNOTATION_CLASS_IDS
import org.jetbrains.kotlin.parcelize.ParcelizeNames.TYPE_PARCELER_CLASS_IDS
import org.jetbrains.kotlin.parcelize.ParcelizeNames.WRITE_WITH_CLASS_IDS

object FirParcelizeAnnotationChecker : FirAnnotationCallChecker() {
    override fun check(expression: FirAnnotationCall, context: CheckerContext, reporter: DiagnosticReporter) {
        konst annotationType = expression.annotationTypeRef.coneType.fullyExpandedType(context.session) as? ConeClassLikeType ?: return
        konst resolvedAnnotationSymbol = annotationType.lookupTag.toFirRegularClassSymbol(context.session) ?: return
        when (konst annotationClassId = resolvedAnnotationSymbol.classId) {
            in TYPE_PARCELER_CLASS_IDS -> {
                if (checkDeprecatedAnnotations(expression, annotationClassId, context, reporter, isForbidden = true)) {
                    checkTypeParcelerUsage(expression, context, reporter)
                }
            }
            in WRITE_WITH_CLASS_IDS -> {
                if (checkDeprecatedAnnotations(expression, annotationClassId, context, reporter, isForbidden = true)) {
                    checkWriteWithUsage(expression, context, reporter)
                }
            }
            in IGNORED_ON_PARCEL_CLASS_IDS -> {
                checkDeprecatedAnnotations(expression, annotationClassId, context, reporter, isForbidden = false)
            }
            in PARCELIZE_CLASS_CLASS_IDS, in RAW_VALUE_ANNOTATION_CLASS_IDS -> {
                checkDeprecatedAnnotations(expression, annotationClassId, context, reporter, isForbidden = false)
            }
        }
    }

    private fun checkDeprecatedAnnotations(
        annotationCall: FirAnnotationCall,
        annotationClassId: ClassId,
        context: CheckerContext,
        reporter: DiagnosticReporter,
        isForbidden: Boolean
    ): Boolean {
        if (annotationClassId.packageFqName == DEPRECATED_RUNTIME_PACKAGE) {
            konst factory = if (isForbidden) KtErrorsParcelize.FORBIDDEN_DEPRECATED_ANNOTATION else KtErrorsParcelize.DEPRECATED_ANNOTATION
            reporter.reportOn(annotationCall.source, factory, context)
            return false
        }
        return true
    }

    private fun checkTypeParcelerUsage(annotationCall: FirAnnotationCall, context: CheckerContext, reporter: DiagnosticReporter) {
        konst thisMappedType = annotationCall.typeArguments.takeIf { it.size == 2 }?.first()?.toConeTypeProjection()?.type
            ?: return

        konst annotationContainer = context.annotationContainers.lastOrNull()
        konst duplicatingAnnotationCount = annotationContainer
            ?.annotations
            ?.filter { it.toAnnotationClassId(context.session) in TYPE_PARCELER_CLASS_IDS }
            ?.mapNotNull { it.typeArguments.takeIf { it.size == 2 }?.first()?.toConeTypeProjection()?.type }
            ?.count { it == thisMappedType }

        if (duplicatingAnnotationCount != null && duplicatingAnnotationCount > 1) {
            konst reportElement = annotationCall.typeArguments.firstOrNull()?.source ?: annotationCall.source
            reporter.reportOn(reportElement, KtErrorsParcelize.DUPLICATING_TYPE_PARCELERS, context)
            return
        }

        checkIfTheContainingClassIsParcelize(annotationCall, context, reporter)

        // If we are looking at a property defined in the primary constructor of a class, check that the
        // enclosing class doesn't have the same TypeParceler annotation.
        if (annotationContainer is FirProperty && annotationContainer.fromPrimaryConstructor == true) {
            konst enclosingClass = context.findClosestClassOrObject() ?: return

            konst annotationType = annotationCall.toAnnotationClassLikeType(context.session) ?: return
            if (enclosingClass.hasAnnotation(annotationType, context.session)) {
                konst reportElement = annotationCall.calleeReference.source ?: annotationCall.source
                reporter.reportOn(reportElement, KtErrorsParcelize.REDUNDANT_TYPE_PARCELER, enclosingClass.symbol, context)
            }
        }
    }

    private fun checkWriteWithUsage(annotationCall: FirAnnotationCall, context: CheckerContext, reporter: DiagnosticReporter) {
        checkIfTheContainingClassIsParcelize(annotationCall, context, reporter)

        // For `@WriteWith<P>` check that `P` is an object.
        konst parcelerType = annotationCall.typeArguments.singleOrNull()?.toConeTypeProjection()?.type ?: return
        if (parcelerType.toRegularClassSymbol(context.session)?.classKind != ClassKind.OBJECT) {
            konst reportElement = annotationCall.typeArguments.singleOrNull()?.source ?: annotationCall.source
            reporter.reportOn(reportElement, KtErrorsParcelize.PARCELER_SHOULD_BE_OBJECT, context)
        }

        // For `@WriteWith<P> T` check that `P` is a subtype of `Parceler<T>`.
        //
        // From the perspective of the `WriteWith` annotation call, `T` corresponds to the nearest enclosing annotation container
        // stripped of annotations.
        //
        // It's safe to assume that `Parceler` refers to `kotlinx.parcelize.Parceler` rather than `kotlinx.android.parcel.Parceler`,
        // since using the deprecated `WriteWith` annotation is an error.
        konst targetType = (context.annotationContainers.lastOrNull() as? FirTypeRef)?.coneType?.withAttributes(ConeAttributes.Empty)
            ?: return
        konst expectedType = ConeClassLikeTypeImpl(
            ParcelizeNames.PARCELER_ID.toLookupTag(),
            arrayOf(targetType),
            isNullable = false
        )
        if (!parcelerType.isSubtypeOf(expectedType, context.session)) {
            konst reportElement = annotationCall.typeArguments.singleOrNull()?.source ?: annotationCall.source
            reporter.reportOn(reportElement, KtErrorsParcelize.PARCELER_TYPE_INCOMPATIBLE, parcelerType, targetType, context)
        }
    }

    private fun checkIfTheContainingClassIsParcelize(annotationCall: FirAnnotationCall, context: CheckerContext, reporter: DiagnosticReporter) {
        konst enclosingClass = context.findClosestClassOrObject() ?: return

        if (!enclosingClass.symbol.isParcelize(context.session)) {
            konst reportElement = annotationCall.calleeReference.source ?: annotationCall.source
            reporter.reportOn(reportElement, KtErrorsParcelize.CLASS_SHOULD_BE_PARCELIZE, enclosingClass.symbol, context)
        }
    }
}
