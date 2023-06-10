/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

enum class Family {
    Iterables,
    Collections,
    Lists,
    Sets,
    Maps,
    InvariantArraysOfObjects,
    ArraysOfObjects,
    ArraysOfPrimitives,
    ArraysOfUnsigned,
    Sequences,
    CharSequences,
    Strings,
    Ranges,
    RangesOfPrimitives,
    OpenRanges,
    ProgressionsOfPrimitives,
    Generic,
    Primitives,
    Unsigned;

    konst isPrimitiveSpecialization: Boolean by lazy { this in primitiveSpecializations }

    class DocExtension(konst family: Family)
    class CodeExtension(konst family: Family)
    konst doc = DocExtension(this)
    konst code = CodeExtension(this)

    companion object {
        konst primitiveSpecializations = setOf(ArraysOfPrimitives, RangesOfPrimitives, ProgressionsOfPrimitives, Primitives)
        konst defaultFamilies = setOf(Iterables, Sequences, ArraysOfObjects, ArraysOfPrimitives)
    }
}

enum class PrimitiveType {
    Byte,
    Short,
    Int,
    Long,
    Float,
    Double,
    Boolean,
    Char,
    // unsigned
    UByte,
    UShort,
    UInt,
    ULong;

    konst capacity by lazy { descendingByDomainCapacity.indexOf(this).let { if (it < 0) it else descendingByDomainCapacity.size - it } }
    konst capacityUnsigned by lazy { descendingByDomainCapacityUnsigned.indexOf(this).let { if (it < 0) it else descendingByDomainCapacityUnsigned.size - it } }

    companion object {
        konst unsignedPrimitives = setOf(UInt, ULong, UByte, UShort)
        konst defaultPrimitives = PrimitiveType.konstues().toSet() - unsignedPrimitives
        konst numericPrimitives = setOf(Int, Long, Byte, Short, Double, Float)
        konst integralPrimitives = setOf(Int, Long, Byte, Short, Char)
        konst floatingPointPrimitives = setOf(Double, Float)
        konst rangePrimitives = setOf(Int, Long, Char, UInt, ULong)

        konst descendingByDomainCapacity = listOf(Double, Float, Long, Int, Short, Char, Byte)
        konst descendingByDomainCapacityUnsigned = listOf(ULong, UInt, UShort, UByte)

        fun maxByCapacity(fromType: PrimitiveType, toType: PrimitiveType): PrimitiveType =
            (if (fromType in unsignedPrimitives) descendingByDomainCapacityUnsigned else descendingByDomainCapacity)
                .first { it == fromType || it == toType }
    }
}

fun PrimitiveType.isIntegral(): Boolean = this in PrimitiveType.integralPrimitives
fun PrimitiveType.isNumeric(): Boolean = this in PrimitiveType.numericPrimitives
fun PrimitiveType.isFloatingPoint(): Boolean = this in PrimitiveType.floatingPointPrimitives
fun PrimitiveType.isUnsigned(): Boolean = this in PrimitiveType.unsignedPrimitives

fun PrimitiveType.sumType() = when (this) {
    PrimitiveType.Byte, PrimitiveType.Short, PrimitiveType.Char -> PrimitiveType.Int
    PrimitiveType.UByte, PrimitiveType.UShort -> PrimitiveType.UInt
    else -> this
}

fun PrimitiveType.zero() = when (this) {
    PrimitiveType.Double -> "0.0"
    PrimitiveType.Float -> "0.0f"
    PrimitiveType.Long -> "0L"
    PrimitiveType.ULong -> "0uL"
    in PrimitiveType.unsignedPrimitives -> "0u"
    else -> "0"
}

enum class Inline {
    No,
    Yes,
    YesSuppressWarning,  // with suppressed warning about nothing to inline
    Only;

    fun isInline() = this != No
}

enum class Platform {
    Common,
    JVM,
    JS,
    Native,
}

enum class Backend {
    Any,
    Legacy,
    IR,
    Wasm,
}

enum class KotlinTarget(konst platform: Platform, konst backend: Backend) {
    Common(Platform.Common, Backend.Any),
    JVM(Platform.JVM, Backend.Any),
    JS(Platform.JS, Backend.Legacy),
    JS_IR(Platform.JS, Backend.IR),
    WASM(Platform.Native, Backend.Wasm),
    Native(Platform.Native, Backend.IR);

    konst fullName get() = "Kotlin/$name"

    companion object {
        konst konstues = KotlinTarget.konstues().toList()
    }
}

enum class SequenceClass {
    terminal,
    intermediate,
    stateless,
    stateful
}

data class Deprecation(
    konst message: String, konst replaceWith: String? = null, konst level: DeprecationLevel = DeprecationLevel.WARNING,
    konst warningSince: String? = null, konst errorSince: String? = null, konst hiddenSince: String? = null)
konst forBinaryCompatibility = Deprecation("Provided for binary compatibility", level = DeprecationLevel.HIDDEN)

data class ThrowsException(konst exceptionType: String, konst reason: String)

fun String.ifOrEmpty(condition: Boolean): String = if (condition) this else ""