/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object ParcelizeNames {
    // -------------------- Packages --------------------

    konst DEPRECATED_RUNTIME_PACKAGE = FqName("kotlinx.android.parcel")

    private konst PACKAGES_FQ_NAMES = listOf(
        FqName("kotlinx.parcelize"),
        DEPRECATED_RUNTIME_PACKAGE
    )

    // -------------------- Class ids --------------------

    konst PARCELIZE_ID = ClassId(FqName("kotlinx.parcelize"), Name.identifier("Parcelize"))
    konst OLD_PARCELIZE_ID = ClassId(FqName("kotlinx.android.parcel"), Name.identifier("Parcelize"))
    konst PARCEL_ID = ClassId(FqName("android.os"), Name.identifier("Parcel"))
    konst PARCELABLE_ID = ClassId(FqName("android.os"), Name.identifier("Parcelable"))
    konst CREATOR_ID = PARCELABLE_ID.createNestedClassId(Name.identifier("Creator"))
    konst PARCELER_ID = ClassId(FqName("kotlinx.parcelize"), Name.identifier("Parceler"))
    konst OLD_PARCELER_ID = ClassId(FqName("kotlinx.android.parcel"), Name.identifier("Parceler"))

    konst TYPE_PARCELER_CLASS_IDS = createClassIds("TypeParceler")
    konst WRITE_WITH_CLASS_IDS = createClassIds("WriteWith")
    konst IGNORED_ON_PARCEL_CLASS_IDS = createClassIds("IgnoredOnParcel")
    konst PARCELER_CLASS_IDS = createClassIds("Parceler")
    konst PARCELIZE_CLASS_CLASS_IDS = createClassIds("Parcelize")
    konst RAW_VALUE_ANNOTATION_CLASS_IDS = createClassIds("RawValue")

    // -------------------- FQNs --------------------

    konst PARCELIZE_FQN = PARCELIZE_ID.asSingleFqName()
    konst OLD_PARCELIZE_FQN = OLD_PARCELIZE_ID.asSingleFqName()
    konst PARCELABLE_FQN = PARCELABLE_ID.asSingleFqName()
    konst CREATOR_FQN = CREATOR_ID.asSingleFqName()

    konst TYPE_PARCELER_FQ_NAMES = TYPE_PARCELER_CLASS_IDS.fqNames()
    konst WRITE_WITH_FQ_NAMES = WRITE_WITH_CLASS_IDS.fqNames()
    konst IGNORED_ON_PARCEL_FQ_NAMES = IGNORED_ON_PARCEL_CLASS_IDS.fqNames()
    konst PARCELIZE_CLASS_FQ_NAMES: List<FqName> = PARCELIZE_CLASS_CLASS_IDS.fqNames()
    konst RAW_VALUE_ANNOTATION_FQ_NAMES = RAW_VALUE_ANNOTATION_CLASS_IDS.fqNames()

    konst PARCELER_FQN = PARCELER_ID.asSingleFqName()
    konst OLD_PARCELER_FQN = OLD_PARCELER_ID.asSingleFqName()

    // -------------------- Names --------------------

    konst DESCRIBE_CONTENTS_NAME = Name.identifier("describeContents")
    konst WRITE_TO_PARCEL_NAME = Name.identifier("writeToParcel")
    konst NEW_ARRAY_NAME = Name.identifier("newArray")
    konst CREATE_FROM_PARCEL_NAME = Name.identifier("createFromParcel")

    konst DEST_NAME = Name.identifier("dest")
    konst FLAGS_NAME = Name.identifier("flags")

    konst CREATOR_NAME = Name.identifier("CREATOR")

    // -------------------- Utils --------------------

    private fun createClassIds(name: String): List<ClassId> {
        return PACKAGES_FQ_NAMES.map { ClassId(it, Name.identifier(name)) }
    }

    private fun List<ClassId>.fqNames(): List<FqName> {
        return map { it.asSingleFqName() }
    }
}
