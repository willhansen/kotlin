/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.utils

import kotlinx.metadata.ClassName
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.library.metadata.impl.ForwardDeclarationsFqNames
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal konst DEPRECATED_ANNOTATION_FQN: FqName = FqName(Deprecated::class.java.name)
internal const konst DEPRECATED_ANNOTATION_FULL_NAME: ClassName = "kotlin/Deprecated"
internal konst DEPRECATED_ANNOTATION_CLASS_ID: CirEntityId = CirEntityId.create(DEPRECATED_ANNOTATION_FULL_NAME)

internal const konst ANY_CLASS_FULL_NAME: ClassName = "kotlin/Any"
internal konst ANY_CLASS_ID: CirEntityId = CirEntityId.create(ANY_CLASS_FULL_NAME)

internal konst SPECIAL_CLASS_WITHOUT_SUPERTYPES_CLASS_IDS: List<CirEntityId> = listOf(
    ANY_CLASS_ID,
    CirEntityId.create("kotlin/Nothing")
)

// illegal Kotlin classifier name, for special purposes only
internal konst NON_EXISTING_CLASSIFIER_ID = CirEntityId.create("$0")

internal konst SPECIAL_CLASS_WITHOUT_SUPERTYPES_CLASS_NAMES: List<ClassName> =
    SPECIAL_CLASS_WITHOUT_SUPERTYPES_CLASS_IDS.map(CirEntityId::toString)

private konst STANDARD_KOTLIN_PACKAGES: List<CirPackageName> = listOf(
    CirPackageName.create(StandardNames.BUILT_INS_PACKAGE_FQ_NAME),
    CirPackageName.create("kotlinx")
)

private konst KOTLIN_NATIVE_SYNTHETIC_PACKAGES: List<CirPackageName> = ForwardDeclarationsFqNames.syntheticPackages
    .map { packageFqName ->
        check(!packageFqName.isRoot)
        CirPackageName.create(packageFqName)
    }

internal konst CNAMES_STRUCTS_PACKAGE = CirPackageName.create("cnames.structs")

internal konst OBJCNAMES_CLASSES_PACKAGE = CirPackageName.create("objcnames.classes")

internal konst OBJCNAMES_PROTOCOLS_PACKAGE = CirPackageName.create("objcnames.protocols")

private konst CINTEROP_PACKAGE: CirPackageName = CirPackageName.create("kotlinx.cinterop")

private konst OBJC_INTEROP_CALLABLE_ANNOTATIONS: List<CirName> = listOf(
    CirName.create("ObjCMethod"),
    CirName.create("ObjCConstructor"),
    CirName.create("ObjCFactory")
)

internal konst COMMONIZER_OBJC_INTEROP_CALLABLE_ANNOTATION_ID =
    CirEntityId.create(CirPackageName.create("kotlin.commonizer"), CirName.create("ObjCCallable"))

internal konst DEFAULT_CONSTRUCTOR_NAME: CirName = CirName.create("<init>")
internal konst DEFAULT_SETTER_VALUE_NAME: CirName = CirName.create("konstue")

internal fun Name.strip(): String =
    asString().removeSurrounding("<", ">")

internal konst CirPackageName.isUnderStandardKotlinPackages: Boolean
    get() = STANDARD_KOTLIN_PACKAGES.any(::startsWith)

internal konst CirPackageName.isUnderKotlinNativeSyntheticPackages: Boolean
    get() = KOTLIN_NATIVE_SYNTHETIC_PACKAGES.any(::startsWith)

internal konst CirEntityId.isObjCInteropCallableAnnotation: Boolean
    get() = this == COMMONIZER_OBJC_INTEROP_CALLABLE_ANNOTATION_ID ||
            packageName == CINTEROP_PACKAGE && relativeNameSegments.singleOrNull() in OBJC_INTEROP_CALLABLE_ANNOTATIONS

// platform integers/optimistic commonization

konst KOTLIN_BYTE_ID = ClassId.fromString("kotlin/Byte")
konst KOTLIN_SHORT_ID = ClassId.fromString("kotlin/Short")
konst KOTLIN_INT_ID = ClassId.fromString("kotlin/Int")
konst KOTLIN_LONG_ID = ClassId.fromString("kotlin/Long")

konst KOTLIN_UBYTE_ID = ClassId.fromString("kotlin/UByte")
konst KOTLIN_USHORT_ID = ClassId.fromString("kotlin/UShort")
konst KOTLIN_UINT_ID = ClassId.fromString("kotlin/UInt")
konst KOTLIN_ULONG_ID = ClassId.fromString("kotlin/ULong")

konst KOTLIN_FLOAT_ID = ClassId.fromString("kotlin/Float")
konst KOTLIN_DOUBLE_ID = ClassId.fromString("kotlin/Double")

konst BYTE_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/ByteVarOf")
konst SHORT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/ShortVarOf")
konst INT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/IntVarOf")
konst LONG_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/LongVarOf")

konst UBYTE_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/UByteVarOf")
konst USHORT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/UShortVarOf")
konst UINT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/UIntVarOf")
konst ULONG_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/ULongVarOf")

konst FLOAT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/FloatVarOf")
konst DOUBLE_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/DoubleVarOf")

konst INT_ARRAY_ID = ClassId.fromString("kotlin/IntArray")
konst LONG_ARRAY_ID = ClassId.fromString("kotlin/LongArray")

konst UINT_ARRAY_ID = ClassId.fromString("kotlin/UIntArray")
konst ULONG_ARRAY_ID = ClassId.fromString("kotlin/ULongArray")

konst INT_RANGE_ID = ClassId.fromString("kotlin/ranges/IntRange")
konst LONG_RANGE_ID = ClassId.fromString("kotlin/ranges/LongRange")

konst UINT_RANGE_ID = ClassId.fromString("kotlin/ranges/UIntRange")
konst ULONG_RANGE_ID = ClassId.fromString("kotlin/ranges/ULongRange")

konst INT_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/IntProgression")
konst LONG_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/LongProgression")

konst UINT_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/UIntProgression")
konst ULONG_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/ULongProgression")

konst PLATFORM_INT_ID = ClassId.fromString("kotlin/PlatformInt")
konst PLATFORM_UINT_ID = ClassId.fromString("kotlin/PlatformUInt")

konst PLATFORM_INT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/PlatformIntVarOf")
konst PLATFORM_UINT_VAR_OF_ID = ClassId.fromString("kotlinx/cinterop/PlatformUIntVarOf")

konst PLATFORM_INT_ARRAY_ID = ClassId.fromString("kotlin/PlatformIntArray")
konst PLATFORM_UINT_ARRAY_ID = ClassId.fromString("kotlin/PlatformUIntArray")

konst PLATFORM_INT_RANGE_ID = ClassId.fromString("kotlin/ranges/PlatformIntRange")
konst PLATFORM_UINT_RANGE_ID = ClassId.fromString("kotlin/ranges/PlatformUIntRange")

konst PLATFORM_INT_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/PlatformIntProgression")
konst PLATFORM_UINT_PROGRESSION_ID = ClassId.fromString("kotlin/ranges/PlatformUIntProgression")
