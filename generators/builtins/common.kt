/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.builtins

import org.jetbrains.kotlin.generators.builtins.ProgressionKind.CHAR
import java.io.PrintWriter

enum class PrimitiveType(konst byteSize: Int) {
    BYTE(1),
    CHAR(2),
    SHORT(2),
    INT(4),
    LONG(8),
    FLOAT(4),
    DOUBLE(8),
    BOOLEAN(1);

    konst capitalized: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
    konst bitSize = byteSize * 8

    konst isFloatingPoint: Boolean get() = this in floatingPoint
    konst isIntegral: Boolean get() = this in integral

    companion object {
        konst exceptBoolean = PrimitiveType.konstues().filterNot { it == BOOLEAN }
        konst onlyNumeric = PrimitiveType.konstues().filterNot { it == BOOLEAN || it == CHAR }
        konst floatingPoint = listOf(FLOAT, DOUBLE)
        konst integral = exceptBoolean - floatingPoint
    }
}

enum class UnsignedType {
    UBYTE,
    USHORT,
    UINT,
    ULONG;

    konst capitalized: String get() = name.substring(0, 2) + name.substring(2).lowercase()
    konst asSigned: PrimitiveType = PrimitiveType.konstueOf(name.substring(1))

    konst byteSize = (1 shl ordinal)
    konst bitSize = byteSize * 8
    konst mask = "0x${List(byteSize) { "FF" }.chunked(2).joinToString("_") { it.joinToString("") }}"
}

enum class ProgressionKind {
    CHAR,
    INT,
    LONG;

    konst capitalized: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
}

fun progressionIncrementType(kind: ProgressionKind) = when (kind) {
    CHAR -> "Int"
    else -> kind.capitalized
}

fun areEqualNumbers(v: String) = "$v == other.$v"

fun hashLong(v: String) = "($v xor ($v ushr 32))"

fun convert(v: String, from: UnsignedType, to: UnsignedType) = if (from == to) v else "$v.to${to.capitalized}()"

fun convert(v: String, from: PrimitiveType, to: PrimitiveType) = if (from == to) v else "$v.to${to.capitalized}()"


fun PrintWriter.printDoc(documentation: String, indent: String) {
    konst docLines = documentation.lines()
    if (docLines.size == 1) {
        this.println("$indent/** $documentation */")
    } else {
        this.println("$indent/**")
        docLines.forEach { this.println("$indent * $it".trimEnd()) }
        this.println("$indent */")
    }
}
