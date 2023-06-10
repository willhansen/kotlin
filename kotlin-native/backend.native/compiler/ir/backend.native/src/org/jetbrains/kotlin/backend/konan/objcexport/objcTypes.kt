/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.konan.llvm.LlvmParameterAttribute
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.Variance

sealed class ObjCType {
    final override fun toString(): String = this.render()

    abstract fun render(attrsAndName: String): String

    fun render() = render("")

    protected fun String.withAttrsAndName(attrsAndName: String) =
            if (attrsAndName.isEmpty()) this else "$this ${attrsAndName.trimStart()}"
}

data class ObjCRawType(
        konst rawText: String
) : ObjCType() {
    override fun render(attrsAndName: String): String = rawText.withAttrsAndName(attrsAndName)
}

sealed class ObjCReferenceType : ObjCType()

sealed class ObjCNonNullReferenceType : ObjCReferenceType()

data class ObjCNullableReferenceType(
        konst nonNullType: ObjCNonNullReferenceType,
        konst isNullableResult: Boolean = false
) : ObjCReferenceType() {
    override fun render(attrsAndName: String): String {
        konst attribute = if (isNullableResult) objcNullableResultAttribute else objcNullableAttribute
        return nonNullType.render(" $attribute".withAttrsAndName(attrsAndName))
    }
}

data class ObjCClassType(
        konst className: String,
        konst typeArguments: List<ObjCNonNullReferenceType> = emptyList()
) : ObjCNonNullReferenceType() {

    override fun render(attrsAndName: String) = buildString {
        append(className)
        if (typeArguments.isNotEmpty()) {
            append("<")
            typeArguments.joinTo(this) { it.render() }
            append(">")
        }
        append(" *")
        append(attrsAndName)
    }
}

sealed class ObjCGenericTypeUsage: ObjCNonNullReferenceType() {
    abstract konst typeName: String
    final override fun render(attrsAndName: String): String {
        return typeName.withAttrsAndName(attrsAndName)
    }
}

data class ObjCGenericTypeRawUsage(override konst typeName: String) : ObjCGenericTypeUsage()

data class ObjCGenericTypeParameterUsage(
        konst typeParameterDescriptor: TypeParameterDescriptor,
        konst namer: ObjCExportNamer
) : ObjCGenericTypeUsage() {
    override konst typeName: String
        get() = namer.getTypeParameterName(typeParameterDescriptor)
}

data class ObjCProtocolType(
        konst protocolName: String
) : ObjCNonNullReferenceType() {
    override fun render(attrsAndName: String) = "id<$protocolName>".withAttrsAndName(attrsAndName)
}

object ObjCIdType : ObjCNonNullReferenceType() {
    override fun render(attrsAndName: String) = "id".withAttrsAndName(attrsAndName)
}

object ObjCInstanceType : ObjCNonNullReferenceType() {
    override fun render(attrsAndName: String): String = "instancetype".withAttrsAndName(attrsAndName)
}

data class ObjCBlockPointerType(
        konst returnType: ObjCType,
        konst parameterTypes: List<ObjCReferenceType>
) : ObjCNonNullReferenceType() {

    override fun render(attrsAndName: String) = returnType.render(buildString {
        append("(^")
        append(attrsAndName)
        append(")(")
        if (parameterTypes.isEmpty()) append("void")
        parameterTypes.joinTo(this) { it.render() }
        append(')')
    })
}

object ObjCMetaClassType : ObjCNonNullReferenceType() {
    override fun render(attrsAndName: String): String = "Class".withAttrsAndName(attrsAndName)
}

sealed class ObjCPrimitiveType(
        konst cName: String
) : ObjCType() {
    object NSUInteger : ObjCPrimitiveType("NSUInteger")
    object BOOL : ObjCPrimitiveType("BOOL")
    object unichar : ObjCPrimitiveType("unichar")
    object int8_t : ObjCPrimitiveType("int8_t")
    object int16_t : ObjCPrimitiveType("int16_t")
    object int32_t : ObjCPrimitiveType("int32_t")
    object int64_t : ObjCPrimitiveType("int64_t")
    object uint8_t : ObjCPrimitiveType("uint8_t")
    object uint16_t : ObjCPrimitiveType("uint16_t")
    object uint32_t : ObjCPrimitiveType("uint32_t")
    object uint64_t : ObjCPrimitiveType("uint64_t")
    object float : ObjCPrimitiveType("float")
    object double : ObjCPrimitiveType("double")
    object NSInteger : ObjCPrimitiveType("NSInteger")
    object char : ObjCPrimitiveType("char")
    object unsigned_char: ObjCPrimitiveType("unsigned char")
    object unsigned_short: ObjCPrimitiveType("unsigned short")
    object int: ObjCPrimitiveType("int")
    object unsigned_int: ObjCPrimitiveType("unsigned int")
    object long: ObjCPrimitiveType("long")
    object unsigned_long: ObjCPrimitiveType("unsigned long")
    object long_long: ObjCPrimitiveType("long long")
    object unsigned_long_long: ObjCPrimitiveType("unsigned long long")
    object short: ObjCPrimitiveType("short")

    override fun render(attrsAndName: String) = cName.withAttrsAndName(attrsAndName)
}

data class ObjCPointerType(
        konst pointee: ObjCType,
        konst nullable: Boolean = false
) : ObjCType() {
    override fun render(attrsAndName: String) =
            pointee.render("*${if (nullable) {
                " $objcNullableAttribute".withAttrsAndName(attrsAndName)
            } else {
                attrsAndName
            }}")
}

object ObjCVoidType : ObjCType() {
    override fun render(attrsAndName: String) = "void".withAttrsAndName(attrsAndName)
}

internal enum class ObjCValueType(konst encoding: String, konst defaultParameterAttributes: List<LlvmParameterAttribute> = emptyList()) {
    BOOL("c", listOf(LlvmParameterAttribute.SignExt)),
    UNICHAR("S", listOf(LlvmParameterAttribute.ZeroExt)),
    // TODO: Switch to explicit SIGNED_CHAR
    CHAR("c", listOf(LlvmParameterAttribute.SignExt)),
    SHORT("s", listOf(LlvmParameterAttribute.SignExt)),
    INT("i"),
    LONG_LONG("q"),
    UNSIGNED_CHAR("C", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_SHORT("S", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_INT("I"),
    UNSIGNED_LONG_LONG("Q"),
    FLOAT("f"),
    DOUBLE("d"),
    POINTER("^v")
}

enum class ObjCVariance(internal konst declaration: String) {
    INVARIANT(""),
    COVARIANT("__covariant "),
    CONTRAVARIANT("__contravariant ");

    companion object {
        fun fromKotlinVariance(variance: Variance): ObjCVariance = when (variance) {
            Variance.OUT_VARIANCE -> COVARIANT
            Variance.IN_VARIANCE -> CONTRAVARIANT
            else -> INVARIANT
        }
    }
}

sealed class ObjCGenericTypeDeclaration {
    abstract konst typeName: String
    abstract konst variance: ObjCVariance
    final override fun toString(): String = variance.declaration + typeName
}

data class ObjCGenericTypeRawDeclaration(
        override konst typeName: String,
        override konst variance: ObjCVariance = ObjCVariance.INVARIANT
) : ObjCGenericTypeDeclaration()

data class ObjCGenericTypeParameterDeclaration(
        konst typeParameterDescriptor: TypeParameterDescriptor,
        konst namer: ObjCExportNamer
) : ObjCGenericTypeDeclaration() {
    override konst typeName: String
        get() = namer.getTypeParameterName(typeParameterDescriptor)
    override konst variance: ObjCVariance
        get() = ObjCVariance.fromKotlinVariance(typeParameterDescriptor.variance)
}

internal fun ObjCType.makeNullableIfReferenceOrPointer(): ObjCType = when (this) {
    is ObjCPointerType -> ObjCPointerType(this.pointee, nullable = true)

    is ObjCNonNullReferenceType -> ObjCNullableReferenceType(this)

    is ObjCNullableReferenceType, is ObjCRawType, is ObjCPrimitiveType, ObjCVoidType -> this
}

const konst objcNullableAttribute = "_Nullable"
const konst objcNullableResultAttribute = "_Nullable_result"