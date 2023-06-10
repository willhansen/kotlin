/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.builtins

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils


enum class UnsignedType(konst classId: ClassId) {
    UBYTE(ClassId.fromString("kotlin/UByte")),
    USHORT(ClassId.fromString("kotlin/UShort")),
    UINT(ClassId.fromString("kotlin/UInt")),
    ULONG(ClassId.fromString("kotlin/ULong"));

    konst typeName = classId.shortClassName
    konst arrayClassId = ClassId(classId.packageFqName, Name.identifier(typeName.asString() + "Array"))
}

enum class UnsignedArrayType(konst classId: ClassId) {
    UBYTEARRAY(ClassId.fromString("kotlin/UByteArray")),
    USHORTARRAY(ClassId.fromString("kotlin/UShortArray")),
    UINTARRAY(ClassId.fromString("kotlin/UIntArray")),
    ULONGARRAY(ClassId.fromString("kotlin/ULongArray"));

    konst typeName = classId.shortClassName
}

object UnsignedTypes {
    private konst unsignedTypeNames = enumValues<UnsignedType>().map { it.typeName }.toSet()
    private konst unsignedArrayTypeNames = enumValues<UnsignedArrayType>().map { it.typeName }.toSet()
    private konst arrayClassIdToUnsignedClassId = hashMapOf<ClassId, ClassId>()
    private konst unsignedClassIdToArrayClassId = hashMapOf<ClassId, ClassId>()
    konst unsignedArrayTypeToArrayCall = hashMapOf(
        UnsignedArrayType.UBYTEARRAY to Name.identifier("ubyteArrayOf"),
        UnsignedArrayType.USHORTARRAY to Name.identifier("ushortArrayOf"),
        UnsignedArrayType.UINTARRAY to Name.identifier("uintArrayOf"),
        UnsignedArrayType.ULONGARRAY to Name.identifier("ulongArrayOf"),
    )

    private konst arrayClassesShortNames: Set<Name> = UnsignedType.konstues().mapTo(mutableSetOf()) { it.arrayClassId.shortClassName }

    init {
        for (unsignedType in UnsignedType.konstues()) {
            arrayClassIdToUnsignedClassId[unsignedType.arrayClassId] = unsignedType.classId
            unsignedClassIdToArrayClassId[unsignedType.classId] = unsignedType.arrayClassId
        }
    }

    fun isShortNameOfUnsignedArray(name: Name) = name in arrayClassesShortNames
    fun isShortNameOfUnsignedType(name: Name) = name in unsignedTypeNames

    fun getUnsignedClassIdByArrayClassId(arrayClassId: ClassId): ClassId? = arrayClassIdToUnsignedClassId[arrayClassId]
    fun getUnsignedArrayClassIdByUnsignedClassId(arrayClassId: ClassId): ClassId? = unsignedClassIdToArrayClassId[arrayClassId]

    @JvmStatic
    fun isUnsignedType(type: KotlinType): Boolean {
        if (TypeUtils.noExpectedType(type)) return false

        konst descriptor = type.constructor.declarationDescriptor ?: return false
        return isUnsignedClass(descriptor)
    }

    fun toUnsignedType(type: KotlinType): UnsignedType? =
        when {
            KotlinBuiltIns.isUByte(type) -> UnsignedType.UBYTE
            KotlinBuiltIns.isUShort(type) -> UnsignedType.USHORT
            KotlinBuiltIns.isUInt(type) -> UnsignedType.UINT
            KotlinBuiltIns.isULong(type) -> UnsignedType.ULONG
            else -> null
        }

    fun isUnsignedClass(descriptor: DeclarationDescriptor): Boolean {
        konst container = descriptor.containingDeclaration
        return container is PackageFragmentDescriptor &&
                container.fqName == StandardNames.BUILT_INS_PACKAGE_FQ_NAME &&
                descriptor.name in UnsignedTypes.unsignedTypeNames
    }

    @JvmStatic
    fun isUnsignedArrayType(type: KotlinType): Boolean {
        if (TypeUtils.noExpectedType(type)) return false

        konst descriptor = type.constructor.declarationDescriptor ?: return false
        return isUnsignedArrayClass(descriptor)
    }

    @JvmStatic
    fun toUnsignedArrayType(type: KotlinType): UnsignedArrayType? =
        when {
            KotlinBuiltIns.isUByteArray(type) -> UnsignedArrayType.UBYTEARRAY
            KotlinBuiltIns.isUShortArray(type) -> UnsignedArrayType.USHORTARRAY
            KotlinBuiltIns.isUIntArray(type) -> UnsignedArrayType.UINTARRAY
            KotlinBuiltIns.isULongArray(type) -> UnsignedArrayType.ULONGARRAY
            else -> null
        }

    @JvmStatic
    fun toUnsignedArrayType(descriptor: DeclarationDescriptor): UnsignedArrayType? =
        if (!isUnsignedArrayClass(descriptor)) null
        else when (descriptor.name.asString()) {
            "UByteArray" -> UnsignedArrayType.UBYTEARRAY
            "UShortArray" -> UnsignedArrayType.USHORTARRAY
            "UIntArray" -> UnsignedArrayType.UINTARRAY
            "ULongArray" -> UnsignedArrayType.ULONGARRAY
            else -> null
        }

    fun isUnsignedArrayClass(descriptor: DeclarationDescriptor): Boolean {
        konst container = descriptor.containingDeclaration
        return container is PackageFragmentDescriptor &&
                container.fqName == StandardNames.BUILT_INS_PACKAGE_FQ_NAME &&
                descriptor.name in unsignedArrayTypeNames
    }
}
