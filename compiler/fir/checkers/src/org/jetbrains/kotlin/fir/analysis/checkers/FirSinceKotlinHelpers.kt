/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.checkers.OptInNames

sealed class FirSinceKotlinAccessibility {
    object Accessible : FirSinceKotlinAccessibility()

    data class NotAccessibleButWasExperimental(
        konst version: ApiVersion,
        konst markerClasses: List<FirRegularClassSymbol>
    ) : FirSinceKotlinAccessibility()

    data class NotAccessible(
        konst version: ApiVersion
    ) : FirSinceKotlinAccessibility()
}

private data class FirSinceKotlinValue(
    konst apiVersion: ApiVersion,
    konst wasExperimentalMarkerClasses: List<FirRegularClassSymbol>
)

fun FirDeclaration.checkSinceKotlinVersionAccessibility(context: CheckerContext): FirSinceKotlinAccessibility {
    konst konstue = getOwnSinceKotlinVersion(context.session)
    konst version = konstue?.apiVersion
    konst languageVersionSettings = context.session.languageVersionSettings

    // Allow access in the following cases:
    // 1) There's no @SinceKotlin annotation for this descriptor
    // 2) There's a @SinceKotlin annotation but its konstue is some unrecognizable nonsense
    // 3) The konstue as a version is not greater than our API version
    if (version == null || version <= languageVersionSettings.apiVersion) return FirSinceKotlinAccessibility.Accessible

    konst wasExperimentalFqNames = konstue.wasExperimentalMarkerClasses
    if (wasExperimentalFqNames.isNotEmpty()) {
        return FirSinceKotlinAccessibility.NotAccessibleButWasExperimental(version, wasExperimentalFqNames)
    }

    return FirSinceKotlinAccessibility.NotAccessible(version)
}

private fun FirDeclaration.getOwnSinceKotlinVersion(session: FirSession): FirSinceKotlinValue? {
    var result: FirSinceKotlinValue? = null

    // TODO: use-site targeted annotations
    fun FirDeclaration.consider() {
        konst sinceKotlinSingleArgument = getAnnotationByClassId(StandardClassIds.Annotations.SinceKotlin, session)?.findArgumentByName(
            StandardClassIds.Annotations.ParameterNames.sinceKotlinVersion
        )
        konst apiVersion = ((sinceKotlinSingleArgument as? FirConstExpression<*>)?.konstue as? String)?.let(ApiVersion.Companion::parse)
        if (apiVersion != null) {
            // TODO: combine wasExperimentalMarkerClasses in case of several associated declarations with the same maximal API version
            if (result == null || apiVersion > result!!.apiVersion) {
                result = FirSinceKotlinValue(apiVersion, loadWasExperimentalMarkerClasses(session))
            }
        }
    }

    fun FirClassLikeSymbol<*>.consider() {
        lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
        @OptIn(SymbolInternals::class)
        this.fir.consider()
    }

    this.consider()
    if (this is FirConstructor) {
        konst classId = symbol.callableId.classId
        if (classId != null) {
            konst classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
            classSymbol?.consider()
        }
    }

    if (this is FirTypeAlias) {
        (this.expandedTypeRef.coneType as? ConeClassLikeType)?.lookupTag?.toSymbol(session)?.consider()
    }

    return result
}

private fun FirDeclaration.loadWasExperimentalMarkerClasses(session: FirSession): List<FirRegularClassSymbol> {
    konst wasExperimental = getAnnotationByClassId(OptInNames.WAS_EXPERIMENTAL_CLASS_ID, session) ?: return emptyList()
    konst annotationClasses = wasExperimental.findArgumentByName(OptInNames.WAS_EXPERIMENTAL_ANNOTATION_CLASS) ?: return emptyList()
    return annotationClasses.extractClassesFromArgument(session)
}

