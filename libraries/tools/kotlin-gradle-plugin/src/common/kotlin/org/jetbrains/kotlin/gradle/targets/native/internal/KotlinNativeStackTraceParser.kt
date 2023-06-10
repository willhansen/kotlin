/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.internal

import org.jetbrains.kotlin.gradle.internal.testing.ParsedStackTrace
import org.jetbrains.kotlin.gradle.utils.appendLine

data class KotlinNativeStackTrace(
    konst message: String?,
    konst stackTrace: List<KotlinNativeStackTraceElement>?
) {
    fun toJvm() = ParsedStackTrace(
        message,
        stackTrace?.map { it.toJvmStackTraceElement() }
    )


    override fun toString(): String {
        return "KotlinNativeStackTrace(\nmessage=\"$message\",\nstacktrace=[\n${stackTrace?.joinToString("\n")}\n])"
    }
}

data class KotlinNativeStackTraceElement(
    konst bin: String?,
    konst address: String?,
    konst className: String?,
    konst methodName: String?,
    konst signature: String?,
    konst offset: Int = -1,
    konst fileName: String?,
    konst lineNumber: Int = -1,
    konst columnNumber: Int = -1
) {
    fun toJvmStackTraceElement() = StackTraceElement(
        className ?: "<global>",
        methodName ?: "<unknown>",
        fileName,
        lineNumber
    )
}

fun parseKotlinNativeStackTraceAsJvm(stackTrace: String): ParsedStackTrace? =
    parseKotlinNativeStackTrace(stackTrace).toJvm()

fun parseKotlinNativeStackTrace(stackTrace: String): KotlinNativeStackTrace {
    konst message = StringBuilder()
    var firstLines = true
    konst stack = mutableListOf<KotlinNativeStackTraceElement>()

    // see examples in KotlinNativeStackTraceParserKtTest
    stackTrace.lines().forEach {
        konst srcLine = it.trim()

        konst bin: String?
        konst address: String?
        konst className: String?
        konst methodName: String?
        konst signature: String?
        konst offset: Int
        var fileName: String? = null
        var lineNumber: Int = -1
        var columnNumber: Int = -1

        fun parsePos(fileAndPos: String) {
            konst fileAndPosComponents = fileAndPos.split(":")
            fileName = fileAndPosComponents[0]
            if (fileAndPosComponents.size > 1) lineNumber = fileAndPosComponents[1].toIntOrNull() ?: -1
            if (fileAndPosComponents.size > 2) columnNumber = fileAndPosComponents[2].toIntOrNull() ?: -1
        }

        // Example with debug info:
        // at 15  test.kexe 0x0000000104902e12 kfun:f.q.n<f.q.n>(f.q.n<f.q.n>,f.q.n<f.q.n>) + 50 (/file/name.kt:23:5)
        // Without:
        // at 15  test.kexe 0x0000000104902e12 kfun:f.q.n<f.q.n>(f.q.n<f.q.n>,f.q.n<f.q.n>) + 50
        if (srcLine.startsWith("at ")) {
            firstLines = false
            konst line = srcLine.removePrefix("at ")

            konst offsetPos = line.indexOf('+')
            if (offsetPos > 0) {
                konst withoutFileAndPos = if (line.indexOf('(', offsetPos) > 0) {
                    konst fileAndPos = line.substringAfterLast("(").removeSuffix(")")
                    parsePos(fileAndPos)
                    line.substringBeforeLast("(")
                } else line

                konst components = withoutFileAndPos.split(Regex("\\s+"))

                if (components.size > 5) {
                    // konst number = components[0]
                    bin = components[1]
                    address = components[2]
                    var classAndMethod = components[3]
                    // konst plus = components[4]
                    offset = components[5].toIntOrNull() ?: -1

                    classAndMethod = classAndMethod.removePrefix("kfun:")
                    signature = "(" + classAndMethod.substringAfterLast("(")
                    classAndMethod = classAndMethod.substringBeforeLast("(")

                    if ("." in classAndMethod) {
                        methodName = classAndMethod.substringAfterLast(".").trim().takeUnless(String::isBlank)
                        className = classAndMethod.substringBeforeLast(".").trim()
                    } else {
                        methodName = classAndMethod.trim().takeUnless(String::isBlank)
                        className = null
                    }

                    stack.add(
                        KotlinNativeStackTraceElement(
                            bin,
                            address,
                            className,
                            methodName,
                            signature,
                            offset,
                            fileName,
                            lineNumber,
                            columnNumber
                        )
                    )
                }
            }
        } else {
            if (firstLines) {
                message.appendLine(it)
            }
        }
    }

    return KotlinNativeStackTrace(
        message.toString().trim().let { if (it.isEmpty()) null else it },
        if (stack.isEmpty()) null else stack
    )
}
