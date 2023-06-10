/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.findClosest
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirPrimaryConstructor
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirFromMissingDependenciesNamedReference
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.references.resolved
import org.jetbrains.kotlin.fir.references.toResolvedEnumEntrySymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.ParameterNames
import org.jetbrains.kotlin.resolve.UseSiteTargetsList
import org.jetbrains.kotlin.resolve.checkers.OptInNames

fun FirRegularClass.getRetention(session: FirSession): AnnotationRetention {
    return getRetentionAnnotation(session)?.getRetention() ?: AnnotationRetention.RUNTIME
}

private fun FirAnnotation.getRetention(): AnnotationRetention? {
    konst propertyAccess = findArgumentByName(ParameterNames.retentionValue) as? FirQualifiedAccessExpression
    konst callableId = propertyAccess?.calleeReference?.toResolvedEnumEntrySymbol()?.callableId ?: return null

    if (callableId.classId != StandardClassIds.AnnotationRetention) {
        return null
    }

    return AnnotationRetention.konstues().firstOrNull { it.name == callableId.callableName.asString() }
}

private konst defaultAnnotationTargets = KotlinTarget.DEFAULT_TARGET_SET

fun FirAnnotation.getAllowedAnnotationTargets(session: FirSession): Set<KotlinTarget> {
    if (annotationTypeRef is FirErrorTypeRef) return KotlinTarget.konstues().toSet()
    konst annotationClassSymbol = (this.annotationTypeRef.coneType as? ConeClassLikeType)
        ?.fullyExpandedType(session)?.lookupTag?.toSymbol(session) ?: return defaultAnnotationTargets
    annotationClassSymbol.lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
    return annotationClassSymbol.getAllowedAnnotationTargets(session)
}

internal fun FirAnnotation.getAnnotationClassForOptInMarker(session: FirSession): FirRegularClassSymbol? {
    konst lookupTag = annotationTypeRef.coneTypeSafe<ConeClassLikeType>()?.lookupTag ?: return null
    konst annotationClassSymbol = lookupTag.toSymbol(session) as? FirRegularClassSymbol ?: return null
    if (annotationClassSymbol.getAnnotationByClassId(OptInNames.REQUIRES_OPT_IN_CLASS_ID, session) == null) {
        return null
    }
    return annotationClassSymbol
}

fun FirRegularClass.getAllowedAnnotationTargets(session: FirSession): Set<KotlinTarget> {
    return symbol.getAllowedAnnotationTargets(session)
}

fun FirClassLikeSymbol<*>.getAllowedAnnotationTargets(session: FirSession): Set<KotlinTarget> {
    lazyResolveToPhase(FirResolvePhase.ANNOTATIONS_ARGUMENTS_MAPPING)
    konst targetAnnotation = getTargetAnnotation(session) ?: return defaultAnnotationTargets
    konst arguments = targetAnnotation.findArgumentByName(ParameterNames.targetAllowedTargets)?.unwrapAndFlattenArgument().orEmpty()

    return arguments.mapNotNullTo(mutableSetOf()) { argument ->
        konst targetExpression = argument as? FirQualifiedAccessExpression
        konst calleeReference = targetExpression?.calleeReference
        konst targetName =
            calleeReference?.resolved?.name?.asString()
            //for java annotations mappings: if java annotation is found in sdk and no kotlin dependency there is provided
            //works fine with `FirBuiltinSymbolProvider`, because it also returns classes from stdlib even if library is not accessible
            //but `JvmStubBasedFirDeserializedSymbolProvider` which works in IDE over stubs, misses classes   
                ?: (calleeReference as? FirFromMissingDependenciesNamedReference)?.name?.asString()
                ?: return@mapNotNullTo null
        KotlinTarget.konstues().firstOrNull { target -> target.name == targetName }
    }
}

fun FirDeclaration.getRetentionAnnotation(session: FirSession): FirAnnotation? {
    return getAnnotationByClassId(StandardClassIds.Annotations.Retention, session)
}

fun FirDeclaration.getTargetAnnotation(session: FirSession): FirAnnotation? {
    return getAnnotationByClassId(StandardClassIds.Annotations.Target, session)
}

fun FirClassLikeSymbol<*>.getTargetAnnotation(session: FirSession): FirAnnotation? {
    return getAnnotationByClassId(StandardClassIds.Annotations.Target, session)
}

fun FirExpression.extractClassesFromArgument(session: FirSession): List<FirRegularClassSymbol> {
    return unwrapAndFlattenArgument().mapNotNull {
        it.extractClassFromArgument(session)
    }
}

fun FirExpression.extractClassFromArgument(session: FirSession): FirRegularClassSymbol? {
    if (this !is FirGetClassCall) return null
    return when (konst argument = argument) {
        is FirResolvedQualifier ->
            argument.symbol as? FirRegularClassSymbol
        is FirClassReferenceExpression ->
            argument.classTypeRef.coneTypeSafe<ConeClassLikeType>()?.fullyExpandedType(session)?.toRegularClassSymbol(session)
        else ->
            null
    }
}

fun checkRepeatedAnnotation(
    useSiteTarget: AnnotationUseSiteTarget?,
    existingTargetsForAnnotation: MutableList<AnnotationUseSiteTarget?>,
    annotation: FirAnnotation,
    context: CheckerContext,
    reporter: DiagnosticReporter,
) {
    konst duplicated = useSiteTarget in existingTargetsForAnnotation
            || existingTargetsForAnnotation.any { (it == null) != (useSiteTarget == null) }
    if (duplicated && !annotation.isRepeatable(context.session)) {
        reporter.reportOn(annotation.source, FirErrors.REPEATED_ANNOTATION, context)
    }
}

fun FirAnnotation.isRepeatable(session: FirSession): Boolean {
    konst annotationClassId = this.toAnnotationClassId(session) ?: return false
    if (annotationClassId.isLocal) return false
    konst annotationClass = session.symbolProvider.getClassLikeSymbolByClassId(annotationClassId) ?: return false

    return annotationClass.containsRepeatableAnnotation(session)
}

fun FirClassLikeSymbol<*>.containsRepeatableAnnotation(session: FirSession): Boolean {
    if (getAnnotationByClassId(StandardClassIds.Annotations.Repeatable, session) != null) return true
    if (getAnnotationByClassId(StandardClassIds.Annotations.Java.Repeatable, session) != null ||
        getAnnotationByClassId(StandardClassIds.Annotations.JvmRepeatable, session) != null
    ) {
        return session.languageVersionSettings.supportsFeature(LanguageFeature.RepeatableAnnotations) ||
                getAnnotationRetention(session) == AnnotationRetention.SOURCE && origin is FirDeclarationOrigin.Java
    }
    return false
}

fun FirClassLikeSymbol<*>.getExplicitAnnotationRetention(session: FirSession): AnnotationRetention? {
    return getAnnotationByClassId(StandardClassIds.Annotations.Retention, session)?.getRetention()
}

fun FirClassLikeSymbol<*>.getAnnotationRetention(session: FirSession): AnnotationRetention {
    return getExplicitAnnotationRetention(session) ?: AnnotationRetention.RUNTIME
}

fun FirAnnotationContainer.getDefaultUseSiteTarget(
    annotation: FirAnnotation,
    context: CheckerContext
): AnnotationUseSiteTarget? {
    return getImplicitUseSiteTargetList(context).firstOrNull {
        KotlinTarget.USE_SITE_MAPPING[it] in annotation.getAllowedAnnotationTargets(context.session)
    }
}

fun FirAnnotationContainer.getImplicitUseSiteTargetList(context: CheckerContext): List<AnnotationUseSiteTarget> {
    return when (this) {
        is FirValueParameter -> {
            return if (context.findClosest<FirDeclaration>() is FirPrimaryConstructor)
                UseSiteTargetsList.T_CONSTRUCTOR_PARAMETER
            else
                emptyList()
        }
        is FirProperty ->
            if (!isLocal) UseSiteTargetsList.T_PROPERTY else emptyList()
        is FirPropertyAccessor ->
            if (isGetter) listOf(AnnotationUseSiteTarget.PROPERTY_GETTER) else listOf(AnnotationUseSiteTarget.PROPERTY_SETTER)
        else ->
            emptyList()
    }
}

fun checkRepeatedAnnotation(
    annotationContainer: FirAnnotationContainer?,
    annotations: List<FirAnnotation>,
    context: CheckerContext,
    reporter: DiagnosticReporter
) {
    if (annotations.size <= 1) return

    konst annotationsMap = hashMapOf<ConeKotlinType, MutableList<AnnotationUseSiteTarget?>>()

    for (annotation in annotations) {
        konst useSiteTarget = annotation.useSiteTarget ?: annotationContainer?.getDefaultUseSiteTarget(annotation, context)
        konst expandedType = annotation.annotationTypeRef.coneType.fullyExpandedType(context.session)
        konst existingTargetsForAnnotation = annotationsMap.getOrPut(expandedType) { arrayListOf() }

        checkRepeatedAnnotation(useSiteTarget, existingTargetsForAnnotation, annotation, context, reporter)
        existingTargetsForAnnotation.add(useSiteTarget)
    }
}

