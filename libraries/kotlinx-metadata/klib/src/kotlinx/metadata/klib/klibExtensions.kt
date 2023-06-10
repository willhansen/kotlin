/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.klib

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.klib.impl.klibExtensions

konst KmFunction.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

var KmFunction.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

var KmFunction.file: KlibSourceFile?
    get() = klibExtensions.file
    set(konstue) {
        klibExtensions.file = konstue
    }

konst KmClass.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

var KmClass.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

var KmClass.file: KlibSourceFile?
    get() = klibExtensions.file
    set(konstue) {
        klibExtensions.file = konstue
    }

konst KmClass.klibEnumEntries: MutableList<KlibEnumEntry>
    get() = klibExtensions.enumEntries

konst KmProperty.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

konst KmProperty.setterAnnotations: MutableList<KmAnnotation>
    get() = klibExtensions.setterAnnotations

konst KmProperty.getterAnnotations: MutableList<KmAnnotation>
    get() = klibExtensions.getterAnnotations

var KmProperty.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

var KmProperty.file: Int?
    get() = klibExtensions.file
    set(konstue) {
        klibExtensions.file = konstue
    }

var KmProperty.compileTimeValue: KmAnnotationArgument?
    get() = klibExtensions.compileTimeValue
    set(konstue) {
        klibExtensions.compileTimeValue = konstue
    }

konst KmType.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

konst KmConstructor.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

var KmConstructor.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

var KmPackage.fqName: String?
    get() = klibExtensions.fqName
    set(konstue) {
        klibExtensions.fqName = konstue
    }

var KmModuleFragment.fqName: String?
    get() = klibExtensions.fqName
    set(konstue) {
        klibExtensions.fqName = konstue
    }

konst KmModuleFragment.className: MutableList<ClassName>
    get() = klibExtensions.className

konst KmModuleFragment.moduleFragmentFiles: MutableList<KlibSourceFile>
    get() = klibExtensions.moduleFragmentFiles

konst KmTypeParameter.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations

var KmTypeParameter.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

var KmTypeAlias.uniqId: UniqId?
    get() = klibExtensions.uniqId
    set(konstue) {
        klibExtensions.uniqId = konstue
    }

konst KmValueParameter.annotations: MutableList<KmAnnotation>
    get() = klibExtensions.annotations
