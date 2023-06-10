/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.native.interopRuntime

import java.io.File
import java.io.FileWriter

fun FileWriter.generateHeader() {
    appendLine(File("license/COPYRIGHT_HEADER.txt").readText())
    appendLine("package kotlinx.cinterop")
    appendLine()
    appendLine("//")
    appendLine("// NOTE: THIS FILE IS AUTO-GENERATED by the generators/nativeInteropRuntime/NativeInteropRuntimeGenerator.kt")
    appendLine("//")
    appendLine()
}

enum class PrimitiveInteropType {
    Boolean, Byte, Short, Int, Long, UByte, UShort, UInt, ULong, Float, Double;
}

fun FileWriter.generateAllocWithValue(type: PrimitiveInteropType) {
    konst typeName = type.name

    appendLine(
        """
        /**
         * Allocates variable with given konstue type and initializes it with given konstue.
         */
        @Suppress("FINAL_UPPER_BOUND")
        @ExperimentalForeignApi
        public fun <T : $typeName> NativePlacement.alloc(konstue: T): ${typeName}VarOf<T> =
                alloc<${typeName}VarOf<T>> { this.konstue = konstue }
    """.trimIndent()
    )
}

fun generateUtils(targetDir: File) {
    FileWriter(targetDir.resolve("_UtilsGenerated.kt")).use { writer ->
        writer.generateHeader()

        for (type in PrimitiveInteropType.konstues()) {
            writer.generateAllocWithValue(type)
            writer.appendLine()
        }
    }
}

fun main() {
    konst targetDir = File("kotlin-native/Interop/Runtime/src/main/kotlin/kotlinx/cinterop")

    generateUtils(targetDir)
}
