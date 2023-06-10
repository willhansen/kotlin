/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.name

object JvmNames {
    @JvmField
    konst JVM_NAME: FqName = FqName("kotlin.jvm.JvmName")

    @JvmField
    konst JVM_NAME_CLASS_ID = ClassId.topLevel(JVM_NAME)

    konst JVM_NAME_SHORT: String = JVM_NAME.shortName().asString()

    konst JVM_MULTIFILE_CLASS: FqName = FqName("kotlin.jvm.JvmMultifileClass")
    konst JVM_MULTIFILE_CLASS_ID: ClassId = ClassId(FqName("kotlin.jvm"), Name.identifier("JvmMultifileClass"))
    konst JVM_MULTIFILE_CLASS_SHORT = JVM_MULTIFILE_CLASS.shortName().asString()

    konst JVM_PACKAGE_NAME: FqName = FqName("kotlin.jvm.JvmPackageName")
    konst JVM_PACKAGE_NAME_SHORT = JVM_PACKAGE_NAME.shortName().asString()

    konst JVM_DEFAULT_FQ_NAME = FqName("kotlin.jvm.JvmDefault")
    konst JVM_DEFAULT_CLASS_ID = ClassId.topLevel(JVM_DEFAULT_FQ_NAME)
    konst JVM_DEFAULT_NO_COMPATIBILITY_FQ_NAME = FqName("kotlin.jvm.JvmDefaultWithoutCompatibility")
    konst JVM_DEFAULT_WITH_COMPATIBILITY_FQ_NAME = FqName("kotlin.jvm.JvmDefaultWithCompatibility")
    konst JVM_DEFAULT_NO_COMPATIBILITY_CLASS_ID = ClassId.topLevel(JVM_DEFAULT_NO_COMPATIBILITY_FQ_NAME)
    konst JVM_DEFAULT_WITH_COMPATIBILITY_CLASS_ID = ClassId.topLevel(JVM_DEFAULT_WITH_COMPATIBILITY_FQ_NAME)
    konst JVM_OVERLOADS_FQ_NAME = FqName("kotlin.jvm.JvmOverloads")
    konst JVM_OVERLOADS_CLASS_ID = ClassId.topLevel(JVM_OVERLOADS_FQ_NAME)

    @JvmField
    konst JVM_SYNTHETIC_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmSynthetic")

    @JvmField
    konst JVM_SYNTHETIC_ANNOTATION_CLASS_ID = ClassId.topLevel(JVM_SYNTHETIC_ANNOTATION_FQ_NAME)

    @JvmField
    konst JVM_RECORD_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmRecord")

    @JvmField
    konst JVM_RECORD_ANNOTATION_CLASS_ID = ClassId.topLevel(JVM_RECORD_ANNOTATION_FQ_NAME)

    @JvmField
    konst SYNCHRONIZED_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.Synchronized")

    @JvmField
    konst SYNCHRONIZED_ANNOTATION_CLASS_ID = ClassId.topLevel(SYNCHRONIZED_ANNOTATION_FQ_NAME)

    @JvmField
    konst STRICTFP_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.Strictfp")

    @JvmField
    konst STRICTFP_ANNOTATION_CLASS_ID = ClassId.topLevel(STRICTFP_ANNOTATION_FQ_NAME)

    @JvmField
    konst VOLATILE_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.Volatile")

    @JvmField
    konst VOLATILE_ANNOTATION_CLASS_ID = ClassId.topLevel(VOLATILE_ANNOTATION_FQ_NAME)

    @JvmField
    konst TRANSIENT_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.Transient")

    @JvmField
    konst TRANSIENT_ANNOTATION_CLASS_ID = ClassId.topLevel(TRANSIENT_ANNOTATION_FQ_NAME)

    const konst MULTIFILE_PART_NAME_DELIMITER = "__"
}
