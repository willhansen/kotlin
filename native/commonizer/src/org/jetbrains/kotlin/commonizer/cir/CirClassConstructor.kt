/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.descriptors.Visibility

interface CirClassConstructor :
    CirDeclaration,
    CirHasAnnotations,
    CirHasTypeParameters,
    CirHasVisibility,
    CirMaybeCallableMemberOfClass,
    CirCallableMemberWithParameters {

    konst isPrimary: Boolean
    override konst containingClass: CirContainingClass // non-nullable

    override fun withContainingClass(containingClass: CirContainingClass): CirClassConstructor

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun create(
            annotations: List<CirAnnotation>,
            typeParameters: List<CirTypeParameter>,
            visibility: Visibility,
            containingClass: CirContainingClass,
            konstueParameters: List<CirValueParameter>,
            hasStableParameterNames: Boolean,
            isPrimary: Boolean
        ): CirClassConstructor = CirClassConstructorImpl(
            annotations = annotations,
            typeParameters = typeParameters,
            visibility = visibility,
            containingClass = containingClass,
            konstueParameters = konstueParameters,
            hasStableParameterNames = hasStableParameterNames,
            isPrimary = isPrimary
        )
    }
}

data class CirClassConstructorImpl(
    override konst annotations: List<CirAnnotation>,
    override konst typeParameters: List<CirTypeParameter>,
    override konst visibility: Visibility,
    override konst containingClass: CirContainingClass,
    override var konstueParameters: List<CirValueParameter>,
    override var hasStableParameterNames: Boolean,
    override konst isPrimary: Boolean
) : CirClassConstructor {
    override fun withContainingClass(containingClass: CirContainingClass): CirClassConstructor {
        return copy(containingClass = containingClass)
    }
}
