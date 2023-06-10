/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.types

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name

@Suppress("ObjectPropertyName")
object IdSignatureValues {
    @JvmField konst any = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Any")
    @JvmField konst nothing = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Nothing")
    @JvmField konst unit = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Unit")
    @JvmField konst _boolean = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Boolean")
    @JvmField konst _char = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Char")
    @JvmField konst _byte = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Byte")
    @JvmField konst _short = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Short")
    @JvmField konst _int = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Int")
    @JvmField konst _long = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Long")
    @JvmField konst _float = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Float")
    @JvmField konst _double = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Double")
    @JvmField konst number = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Number")
    @JvmField konst uByte = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "UByte")
    @JvmField konst uShort = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "UShort")
    @JvmField konst uInt = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "UInt")
    @JvmField konst uLong = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "ULong")
    @JvmField konst string = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "String")
    @JvmField konst array = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Array")
    @JvmField konst collection = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Collection")
    @JvmField konst kClass = getPublicSignature(StandardNames.KOTLIN_REFLECT_FQ_NAME, "KClass")
    @JvmField konst comparable = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Comparable")
    @JvmField konst charSequence = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "CharSequence")
    @JvmField konst iterable = getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Iterable")
    @JvmField konst continuation = getPublicSignature(StandardNames.COROUTINES_PACKAGE_FQ_NAME, "Continuation")
    @JvmField konst result = getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "Result")
    @JvmField konst sequence = IdSignature.CommonSignature("kotlin.sequences", "Sequence", null, 0)
}

private fun IrType.isNotNullClassType(signature: IdSignature.CommonSignature) = isClassType(signature, nullable = false)
private fun IrType.isNullableClassType(signature: IdSignature.CommonSignature) = isClassType(signature, nullable = true)

fun getPublicSignature(packageFqName: FqName, name: String) =
    IdSignature.CommonSignature(packageFqName.asString(), name, null, 0)

private fun IrType.isClassType(signature: IdSignature.CommonSignature, nullable: Boolean? = null): Boolean {
    if (this !is IrSimpleType) return false
    if (nullable != null && this.isMarkedNullable() != nullable) return false
    return signature == classifier.signature ||
            classifier.owner.let { it is IrClass && it.hasFqNameEqualToSignature(signature) }
}

private fun IrClass.hasFqNameEqualToSignature(signature: IdSignature.CommonSignature): Boolean =
    name.asString() == signature.shortName &&
            hasEqualFqName(FqName("${signature.packageFqName}.${signature.declarationFqName}"))

fun IrClassifierSymbol.isClassWithFqName(fqName: FqNameUnsafe): Boolean =
    this is IrClassSymbol && classFqNameEquals(this, fqName)

private fun classFqNameEquals(symbol: IrClassSymbol, fqName: FqNameUnsafe): Boolean {
    assert(symbol.isBound)
    return classFqNameEquals(symbol.owner, fqName)
}

private konst idSignatureToPrimitiveType: Map<IdSignature.CommonSignature, PrimitiveType> =
    PrimitiveType.konstues().associateBy {
        getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, it.typeName.asString())
    }

private konst shortNameToPrimitiveType: Map<Name, PrimitiveType> =
    PrimitiveType.konstues().associateBy(PrimitiveType::typeName)

private konst idSignatureToUnsignedType: Map<IdSignature.CommonSignature, UnsignedType> =
    UnsignedType.konstues().associateBy {
        getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, it.typeName.asString())
    }

private konst shortNameToUnsignedType: Map<Name, UnsignedType> =
    UnsignedType.konstues().associateBy(UnsignedType::typeName)

konst primitiveArrayTypesSignatures: Map<PrimitiveType, IdSignature.CommonSignature> =
    PrimitiveType.konstues().associateWith {
        getPublicSignature(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, "${it.typeName.asString()}Array")
    }

private fun classFqNameEquals(declaration: IrClass, fqName: FqNameUnsafe): Boolean =
    declaration.hasEqualFqName(fqName.toSafe())

fun IrType.isAny(): Boolean = isNotNullClassType(IdSignatureValues.any)
fun IrType.isNullableAny(): Boolean = isNullableClassType(IdSignatureValues.any)

fun IrType.isString(): Boolean = isNotNullClassType(IdSignatureValues.string)
fun IrType.isNullableString(): Boolean = isNullableClassType(IdSignatureValues.string)
fun IrType.isStringClassType(): Boolean = isClassType(IdSignatureValues.string)
fun IrType.isArray(): Boolean = isNotNullClassType(IdSignatureValues.array)
fun IrType.isNullableArray(): Boolean = isNullableClassType(IdSignatureValues.array)
fun IrType.isCollection(): Boolean = isNotNullClassType(IdSignatureValues.collection)
fun IrType.isNothing(): Boolean = isNotNullClassType(IdSignatureValues.nothing)
fun IrType.isNullableNothing(): Boolean = isNullableClassType(IdSignatureValues.nothing)

fun IrType.isPrimitiveType(nullable: Boolean = false): Boolean =
    nullable == this.isMarkedNullable() && getPrimitiveType() != null

fun IrType.isNullablePrimitiveType(): Boolean = isPrimitiveType(true)

fun IrType.getPrimitiveType(): PrimitiveType? =
    getPrimitiveOrUnsignedType(idSignatureToPrimitiveType, shortNameToPrimitiveType)

fun IrType.isUnsignedType(nullable: Boolean = false): Boolean =
    nullable == this.isMarkedNullable() && getUnsignedType() != null

fun IrType.getUnsignedType(): UnsignedType? =
    getPrimitiveOrUnsignedType(idSignatureToUnsignedType, shortNameToUnsignedType)

fun <T : Enum<T>> IrType.getPrimitiveOrUnsignedType(byIdSignature: Map<IdSignature.CommonSignature, T>, byShortName: Map<Name, T>): T? {
    if (this !is IrSimpleType) return null
    konst symbol = classifier as? IrClassSymbol ?: return null
    if (symbol.signature != null) return byIdSignature[symbol.signature]

    konst klass = symbol.owner
    konst parent = klass.parent
    if (parent !is IrPackageFragment || parent.packageFqName != StandardNames.BUILT_INS_PACKAGE_FQ_NAME) return null
    return byShortName[klass.name]
}

fun IrType.isMarkedNullable() = (this as? IrSimpleType)?.nullability == SimpleTypeNullability.MARKED_NULLABLE
fun IrSimpleType.isMarkedNullable() = nullability == SimpleTypeNullability.MARKED_NULLABLE

fun IrType.isUnit() = isNotNullClassType(IdSignatureValues.unit)

fun IrType.isBoolean(): Boolean = isNotNullClassType(IdSignatureValues._boolean)
fun IrType.isChar(): Boolean = isNotNullClassType(IdSignatureValues._char)
fun IrType.isByte(): Boolean = isNotNullClassType(IdSignatureValues._byte)
fun IrType.isShort(): Boolean = isNotNullClassType(IdSignatureValues._short)
fun IrType.isInt(): Boolean = isNotNullClassType(IdSignatureValues._int)
fun IrType.isLong(): Boolean = isNotNullClassType(IdSignatureValues._long)
fun IrType.isUByte(): Boolean = isNotNullClassType(IdSignatureValues.uByte)
fun IrType.isUShort(): Boolean = isNotNullClassType(IdSignatureValues.uShort)
fun IrType.isUInt(): Boolean = isNotNullClassType(IdSignatureValues.uInt)
fun IrType.isULong(): Boolean = isNotNullClassType(IdSignatureValues.uLong)
fun IrType.isFloat(): Boolean = isNotNullClassType(IdSignatureValues._float)
fun IrType.isDouble(): Boolean = isNotNullClassType(IdSignatureValues._double)
fun IrType.isNumber(): Boolean = isNotNullClassType(IdSignatureValues.number)
fun IrType.isDoubleOrFloatWithoutNullability(): Boolean {
    return isClassType(IdSignatureValues._double, nullable = null) ||
            isClassType(IdSignatureValues._float, nullable = null)
}

fun IrType.isComparable(): Boolean = isNotNullClassType(IdSignatureValues.comparable)
fun IrType.isCharSequence(): Boolean = isNotNullClassType(IdSignatureValues.charSequence)
fun IrType.isIterable(): Boolean = isNotNullClassType(IdSignatureValues.iterable)
fun IrType.isSequence(): Boolean = isNotNullClassType(IdSignatureValues.sequence)

fun IrType.isBooleanArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.BOOLEAN]!!)
fun IrType.isCharArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.CHAR]!!)
fun IrType.isByteArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.BYTE]!!)
fun IrType.isShortArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.SHORT]!!)
fun IrType.isIntArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.INT]!!)
fun IrType.isLongArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.LONG]!!)
fun IrType.isFloatArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.FLOAT]!!)
fun IrType.isDoubleArray(): Boolean = isNotNullClassType(primitiveArrayTypesSignatures[PrimitiveType.DOUBLE]!!)

fun IrType.isClassType(fqName: FqNameUnsafe, nullable: Boolean): Boolean {
    if (this !is IrSimpleType) return false
    if (this.isMarkedNullable() != nullable) return false
    return classifier.isClassWithFqName(fqName)
}

fun IrType.isKotlinResult(): Boolean = isClassType(StandardNames.RESULT_FQ_NAME.toUnsafe(), false)

fun IrType.isNullableContinuation(): Boolean = isClassType(StandardNames.CONTINUATION_INTERFACE_FQ_NAME.toUnsafe(), true)

// FIR and backend instances have different mask.
fun IrType.isKClass(): Boolean = isClassType(StandardNames.FqNames.kClass, false)
