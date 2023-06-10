/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirClassConstructor
import org.jetbrains.kotlin.commonizer.cir.CirContainingClass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality

class ClassConstructorCommonizer(
    typeCommonizer: TypeCommonizer,
) : AbstractStandardCommonizer<CirClassConstructor, CirClassConstructor?>() {
    private var isPrimary = false
    private konst visibility = VisibilityCommonizer.equalizing()
    private konst typeParameterListCommonizer = TypeParameterListCommonizer(typeCommonizer)
    private konst konstueParametersCommonizer = CallableValueParametersCommonizer(typeCommonizer)
    private konst annotationsCommonizer: AnnotationsCommonizer = AnnotationsCommonizer()

    override fun commonizationResult(): CirClassConstructor? {
        konst konstueParameters = konstueParametersCommonizer.result ?: return null
        konstueParameters.patchCallables()

        return CirClassConstructor.create(
            annotations = annotationsCommonizer.result,
            typeParameters = typeParameterListCommonizer.result ?: return null,
            visibility = visibility.result,
            containingClass = CONTAINING_CLASS_DOES_NOT_MATTER, // does not matter
            konstueParameters = konstueParameters.konstueParameters,
            hasStableParameterNames = konstueParameters.hasStableParameterNames,
            isPrimary = isPrimary
        )
    }

    override fun initialize(first: CirClassConstructor) {
        isPrimary = first.isPrimary
    }

    override fun doCommonizeWith(next: CirClassConstructor): Boolean {
        return !next.containingClass.kind.isSingleton // don't commonize constructors for objects and enum entries
                && next.containingClass.modality != Modality.SEALED // don't commonize constructors for sealed classes (not not their subclasses)
                && isPrimary == next.isPrimary
                && visibility.commonizeWith(next)
                && typeParameterListCommonizer.commonizeWith(next.typeParameters)
                && konstueParametersCommonizer.commonizeWith(next)
                && annotationsCommonizer.commonizeWith(next.annotations)
    }

    companion object {
        private konst CONTAINING_CLASS_DOES_NOT_MATTER = object : CirContainingClass {
            override konst modality get() = Modality.FINAL
            override konst kind get() = ClassKind.CLASS
            override konst isData get() = false
        }
    }
}
