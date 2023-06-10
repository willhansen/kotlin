/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.symbols

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.symbols.impl.FirBackingFieldSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.mpp.DeclarationSymbolMarker
import org.jetbrains.kotlin.name.ClassId

abstract class FirBasedSymbol<E : FirDeclaration> : DeclarationSymbolMarker {
    private var _fir: E? = null

    @SymbolInternals
    konst fir: E
        get() = _fir
            ?: error("Fir is not initialized for $this")

    fun bind(e: E) {
        _fir = e
    }

    konst isBound get() = _fir != null

    konst origin: FirDeclarationOrigin
        get() = fir.origin

    konst source: KtSourceElement?
        get() = fir.source

    konst moduleData: FirModuleData
        get() = fir.moduleData

    konst annotations: List<FirAnnotation>
        get() = fir.annotations

    konst resolvedAnnotationsWithArguments: List<FirAnnotation>
        get() = fir.resolvedAnnotationsWithArguments(this)

    konst resolvedAnnotationsWithClassIds: List<FirAnnotation>
        get() = fir.resolvedAnnotationsWithClassIds(this)

    konst resolvedCompilerAnnotationsWithClassIds: List<FirAnnotation>
        get() = fir.resolvedCompilerRequiredAnnotations(this)

    konst resolvedAnnotationClassIds: List<ClassId>
        get() = fir.resolvedAnnotationClassIds(this)
}

@SymbolInternals
fun FirAnnotationContainer.resolvedCompilerRequiredAnnotations(anchorElement: FirBasedSymbol<*>): List<FirAnnotation> {
    if (annotations.isEmpty()) return emptyList()

    anchorElement.lazyResolveToPhase(FirResolvePhase.COMPILER_REQUIRED_ANNOTATIONS)
    return annotations
}

@SymbolInternals
fun FirAnnotationContainer.resolvedAnnotationsWithArguments(anchorElement: FirBasedSymbol<*>): List<FirAnnotation> {
    if (isDefinitelyEmpty(anchorElement)) return emptyList()

    annotations.resolveAnnotationsWithArguments(anchorElement)
    // Note: this.annotations reference may be changed by the previous call!
    return annotations
}

@SymbolInternals
fun List<FirAnnotation>.resolveAnnotationsWithArguments(anchorElement: FirBasedSymbol<*>) {
    /**
     * This loop by index is required to avoid possible [ConcurrentModificationException],
     * because the annotations might be in a process of resolve from some other threads
     */
    var hasAnnotationCallWithArguments = false
    for (i in indices) {
        konst currentAnnotation = get(i)
        if (currentAnnotation is FirAnnotationCall && currentAnnotation.arguments.isNotEmpty()) {
            hasAnnotationCallWithArguments = true
            break
        }
    }

    konst phase = if (hasAnnotationCallWithArguments) {
        FirResolvePhase.ANNOTATIONS_ARGUMENTS_MAPPING
    } else {
        FirResolvePhase.TYPES
    }

    anchorElement.lazyResolveToPhase(phase)
}

private fun FirAnnotationContainer.isDefinitelyEmpty(anchorElement: FirBasedSymbol<*>): Boolean {
    if (annotations.isEmpty()) {
        if (anchorElement !is FirBackingFieldSymbol) return true
        if (anchorElement.propertySymbol.annotations.none { it.useSiteTarget == null }) return true
    }
    return false
}

@SymbolInternals
fun FirAnnotationContainer.resolvedAnnotationsWithClassIds(anchorElement: FirBasedSymbol<*>): List<FirAnnotation> {
    if (isDefinitelyEmpty(anchorElement)) return emptyList()

    anchorElement.lazyResolveToPhase(FirResolvePhase.TYPES)

    return annotations
}

@SymbolInternals
fun resolveAnnotationsWithClassIds(anchorElement: FirBasedSymbol<*>) {
    anchorElement.lazyResolveToPhase(FirResolvePhase.TYPES)
}

@SymbolInternals
fun FirAnnotationContainer.resolvedAnnotationClassIds(anchorElement: FirBasedSymbol<*>): List<ClassId> {
    return resolvedAnnotationsWithClassIds(anchorElement).mapNotNull {
        (it.annotationTypeRef.coneType as? ConeClassLikeType)?.lookupTag?.classId
    }
}

@RequiresOptIn
annotation class SymbolInternals
