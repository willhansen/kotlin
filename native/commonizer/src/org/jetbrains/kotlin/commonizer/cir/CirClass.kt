/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility

interface CirClass : CirClassifier, CirContainingClass, AnyClass {
    var companion: CirName? // null means no companion object
    konst isCompanion: Boolean
    konst isValue: Boolean
    konst isInner: Boolean
    konst hasEnumEntries: Boolean
    konst supertypes: List<CirType>

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun create(
            annotations: List<CirAnnotation>,
            name: CirName,
            typeParameters: List<CirTypeParameter>,
            supertypes: List<CirType>,
            visibility: Visibility,
            modality: Modality,
            kind: ClassKind,
            companion: CirName?,
            isCompanion: Boolean,
            isData: Boolean,
            isValue: Boolean,
            isInner: Boolean,
            hasEnumEntries: Boolean
        ): CirClass = CirClassImpl(
            annotations = annotations,
            name = name,
            typeParameters = typeParameters,
            supertypes = supertypes,
            visibility = visibility,
            modality = modality,
            kind = kind,
            companion = companion,
            isCompanion = isCompanion,
            isData = isData,
            isValue = isValue,
            isInner = isInner,
            hasEnumEntries = hasEnumEntries
        )
    }
}

data class CirClassImpl(
    override konst annotations: List<CirAnnotation>,
    override konst name: CirName,
    override konst typeParameters: List<CirTypeParameter>,
    override konst supertypes: List<CirType>,
    override konst visibility: Visibility,
    override konst modality: Modality,
    override konst kind: ClassKind,
    override var companion: CirName?,
    override konst isCompanion: Boolean,
    override konst isData: Boolean,
    override konst isValue: Boolean,
    override konst isInner: Boolean,
    override konst hasEnumEntries: Boolean
) : CirClass
