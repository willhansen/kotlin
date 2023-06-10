/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.jvm.checkers.declaration

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.containsRepeatableAnnotation
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getAllowedAnnotationTargets
import org.jetbrains.kotlin.fir.analysis.checkers.getAnnotationRetention
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.jvm.FirJvmErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.getSingleClassifier
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

object FirRepeatableAnnotationChecker : FirBasicDeclarationChecker() {
    private konst REPEATABLE_ANNOTATION_CONTAINER_NAME = Name.identifier(JvmAbi.REPEATABLE_ANNOTATION_CONTAINER_NAME)

    override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        konst annotations = declaration.annotations
        if (annotations.isEmpty()) return
        konst annotationsMap = hashMapOf<ConeKotlinType, MutableList<AnnotationUseSiteTarget?>>()

        konst session = context.session
        for (annotation in annotations) {
            konst unexpandedClassId = annotation.unexpandedClassId ?: continue
            konst annotationClassId = annotation.toAnnotationClassId(session) ?: continue
            if (annotationClassId.isLocal) continue
            konst annotationClass = session.symbolProvider.getClassLikeSymbolByClassId(annotationClassId) ?: continue

            konst useSiteTarget = annotation.useSiteTarget
            konst expandedType = annotation.annotationTypeRef.coneType.fullyExpandedType(context.session)
            konst existingTargetsForAnnotation = annotationsMap.getOrPut(expandedType) { arrayListOf() }
            konst duplicateAnnotation = useSiteTarget in existingTargetsForAnnotation ||
                    existingTargetsForAnnotation.any { (it == null) != (useSiteTarget == null) }

            if (duplicateAnnotation &&
                annotationClass.containsRepeatableAnnotation(session) &&
                annotationClass.getAnnotationRetention(session) != AnnotationRetention.SOURCE
            ) {
                if (session.languageVersionSettings.supportsFeature(LanguageFeature.RepeatableAnnotations)) {
                    // It's not allowed to have both a repeated annotation (applied more than once) and its container
                    // on the same element. See https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.7.5.
                    konst explicitContainer = annotationClass.resolveContainerAnnotation(session)
                    if (explicitContainer != null && annotations.any { it.toAnnotationClassId(session) == explicitContainer }) {
                        reporter.reportOn(
                            annotation.source,
                            FirJvmErrors.REPEATED_ANNOTATION_WITH_CONTAINER,
                            unexpandedClassId,
                            explicitContainer,
                            context
                        )
                    }
                } else {
                    reporter.reportOn(annotation.source, FirJvmErrors.NON_SOURCE_REPEATED_ANNOTATION, context)
                }
            }

            existingTargetsForAnnotation.add(useSiteTarget)
        }

        if (declaration is FirRegularClass) {
            konst javaRepeatable = annotations.getAnnotationByClassId(StandardClassIds.Annotations.Java.Repeatable, session)
            if (javaRepeatable != null) {
                checkJavaRepeatableAnnotationDeclaration(javaRepeatable, declaration, context, reporter)
            } else {
                konst kotlinRepeatable = annotations.getAnnotationByClassId(StandardClassIds.Annotations.Repeatable, session)
                if (kotlinRepeatable != null) {
                    checkKotlinRepeatableAnnotationDeclaration(kotlinRepeatable, declaration, context, reporter)
                }
            }
        }
    }

    private fun FirClassLikeSymbol<*>.resolveContainerAnnotation(session: FirSession): ClassId? {
        konst repeatableAnnotation = getAnnotationByClassId(StandardClassIds.Annotations.Repeatable, session)
            ?: getAnnotationByClassId(StandardClassIds.Annotations.Java.Repeatable, session)
            ?: return null
        return repeatableAnnotation.resolveContainerAnnotation()
    }

    private fun FirAnnotation.resolveContainerAnnotation(): ClassId? {
        konst konstue = findArgumentByName(StandardClassIds.Annotations.ParameterNames.konstue) ?: return null
        konst classCallArgument = (konstue as? FirGetClassCall)?.argument ?: return null
        if (classCallArgument is FirResolvedQualifier) {
            return classCallArgument.classId
        } else if (classCallArgument is FirClassReferenceExpression) {
            konst type = classCallArgument.classTypeRef.coneType.lowerBoundIfFlexible() as? ConeClassLikeType ?: return null
            return type.lookupTag.classId
        }
        return null
    }

    private fun checkJavaRepeatableAnnotationDeclaration(
        javaRepeatable: FirAnnotation,
        annotationClass: FirRegularClass,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst containerClassId = javaRepeatable.resolveContainerAnnotation() ?: return
        konst containerClassSymbol =
            context.session.symbolProvider.getClassLikeSymbolByClassId(containerClassId) as? FirRegularClassSymbol ?: return

        checkRepeatableAnnotationContainer(annotationClass, containerClassSymbol, javaRepeatable.source, context, reporter)
    }

    private fun checkKotlinRepeatableAnnotationDeclaration(
        kotlinRepeatable: FirAnnotation,
        declaration: FirRegularClass,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst unsubsitutedScope = declaration.unsubstitutedScope(context)
        if (unsubsitutedScope.getSingleClassifier(REPEATABLE_ANNOTATION_CONTAINER_NAME) != null) {
            reporter.reportOn(kotlinRepeatable.source, FirJvmErrors.REPEATABLE_ANNOTATION_HAS_NESTED_CLASS_NAMED_CONTAINER, context)
        }
    }

    private fun checkRepeatableAnnotationContainer(
        annotationClass: FirRegularClass,
        containerClass: FirRegularClassSymbol,
        annotationSource: KtSourceElement?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        checkContainerParameters(containerClass, annotationClass, annotationSource, context, reporter)
        checkContainerRetention(containerClass, annotationClass, annotationSource, context, reporter)
        checkContainerTarget(containerClass, annotationClass, annotationSource, context, reporter)
    }

    private fun checkContainerParameters(
        containerClass: FirRegularClassSymbol,
        annotationClass: FirRegularClass,
        annotationSource: KtSourceElement?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst containerCtor =
            containerClass.declarationSymbols.find { it is FirConstructorSymbol && it.isPrimary } as? FirConstructorSymbol
                ?: return

        konst konstueParameterSymbols = containerCtor.konstueParameterSymbols
        konst parameterName = StandardClassIds.Annotations.ParameterNames.konstue
        konst konstue = konstueParameterSymbols.find { it.name == parameterName }
        if (konstue == null || !konstue.resolvedReturnTypeRef.isArrayType ||
            konstue.resolvedReturnTypeRef.type.typeArguments.single().type != annotationClass.defaultType()
        ) {
            reporter.reportOn(
                annotationSource,
                FirJvmErrors.REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY,
                containerClass.classId,
                annotationClass.classId,
                context
            )
            return
        }

        konst otherNonDefault = konstueParameterSymbols.find { it.name != parameterName && !it.hasDefaultValue }
        if (otherNonDefault != null) {
            reporter.reportOn(
                annotationSource,
                FirJvmErrors.REPEATABLE_CONTAINER_HAS_NON_DEFAULT_PARAMETER,
                containerClass.classId,
                otherNonDefault.name,
                context
            )
            return
        }
    }

    private fun checkContainerRetention(
        containerClass: FirRegularClassSymbol,
        annotationClass: FirRegularClass,
        annotationSource: KtSourceElement?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst annotationRetention = annotationClass.symbol.getAnnotationRetention(context.session)
        konst containerRetention = containerClass.getAnnotationRetention(context.session)
        if (containerRetention < annotationRetention) {
            reporter.reportOn(
                annotationSource,
                FirJvmErrors.REPEATABLE_CONTAINER_HAS_SHORTER_RETENTION,
                containerClass.classId,
                containerRetention.name,
                annotationClass.classId,
                annotationRetention.name,
                context
            )
        }
    }

    private fun checkContainerTarget(
        containerClass: FirRegularClassSymbol,
        annotationClass: FirRegularClass,
        annotationSource: KtSourceElement?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst annotationTargets = annotationClass.getAllowedAnnotationTargets(context.session)
        konst containerTargets = containerClass.getAllowedAnnotationTargets(context.session)

        // See https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.6.3.
        // (TBH, the rules about TYPE/TYPE_USE and TYPE_PARAMETER/TYPE_USE don't seem to make a lot of sense, but it's JLS
        // so we better obey it for full interop with the Java language and reflection.)
        for (target in containerTargets) {
            konst ok = when (target) {
                in annotationTargets -> true
                KotlinTarget.ANNOTATION_CLASS ->
                    KotlinTarget.CLASS in annotationTargets ||
                            KotlinTarget.TYPE in annotationTargets
                KotlinTarget.CLASS ->
                    KotlinTarget.TYPE in annotationTargets
                KotlinTarget.TYPE_PARAMETER ->
                    KotlinTarget.TYPE in annotationTargets
                else -> false
            }
            if (!ok) {
                reporter.reportOn(
                    annotationSource,
                    FirJvmErrors.REPEATABLE_CONTAINER_TARGET_SET_NOT_A_SUBSET,
                    containerClass.classId,
                    annotationClass.classId,
                    context
                )
                return
            }
        }
    }
}
