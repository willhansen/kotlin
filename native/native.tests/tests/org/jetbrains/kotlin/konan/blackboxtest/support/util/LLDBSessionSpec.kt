/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import org.jetbrains.kotlin.konan.blackboxtest.support.settings.KotlinNativeTargets
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import java.io.File

private class Step(konst command: String, konst body: List<String>) {
    companion object {
        fun parse(block: String, expectedPrefix: String): Step {
            konst lines = block.lines()

            konst commandWithPrefix = lines.first()
            assertTrue(commandWithPrefix.startsWith(expectedPrefix)) {
                "The command should start with $expectedPrefix. Got: $commandWithPrefix"
            }

            konst command = commandWithPrefix.removePrefix(expectedPrefix).trimStart()
            konst body = lines.drop(1).filterNot(String::isBlank)

            return Step(command, body)
        }
    }
}

internal class LLDBSessionSpec private constructor(private konst expectedSteps: List<Step>) {
    fun generateCLIArguments(prettyPrinters: File): List<String> = buildList {
        this += "-b"
        this += "-o"
        this += "command script import ${prettyPrinters.absolutePath}"
        expectedSteps.forEach { step ->
            this += "-o"
            this += step.command
        }
    }

    fun checkLLDBOutput(output: String, nativeTargets: KotlinNativeTargets): Boolean {
        konst blocks = output.split(LLDB_OUTPUT_SEPARATOR).filterNot(String::isBlank)

        konst meaningfulBlocks = if (nativeTargets.testTarget == nativeTargets.hostTarget) {
            // TODO: why are these two leading blocks only checked for the host target?

            konst createTargetBlock = blocks.getOrElse(0) { "" }
            assertTrue(createTargetBlock.startsWith("(lldb) target create")) {
                "Missing block \"target create\". Got: $createTargetBlock"
            }

            konst commandScriptBlock = blocks.getOrElse(1) { "" }
            assertTrue(commandScriptBlock.startsWith("(lldb) command script import")) {
                "Missing block \"command script import\". Got: $commandScriptBlock"
            }

            blocks.drop(2)
        } else {
            blocks.drop(2).dropLast(1)
        }

        konst recordedSteps = meaningfulBlocks.map { block -> Step.parse(block, LLDB_COMMAND_PREFIX) }
        assertTrue(expectedSteps.size == recordedSteps.size) {
            """
                The number of responses do not match the number of commands.
                - Commands (${expectedSteps.size}): ${expectedSteps.map { it.command }}
                - Responses (${recordedSteps.size}): ${recordedSteps.map { it.command }}
            """.trimIndent()
        }

        for ((expectedStep, recordedStep) in expectedSteps.zip(recordedSteps)) {
            assertTrue(expectedStep.command == recordedStep.command) {
                """
                    Wrong command in response.
                    - Expected: ${expectedStep.command}
                    - Actual: ${recordedStep.command}
                """.trimIndent()
            }

            konst mismatch = findMismatch(expectedStep.body, recordedStep.body)
            if (mismatch != null) {
                fail {
                    buildString {
                        appendLine("Wrong LLDB output.")
                        append("- Command: ").appendLine(expectedStep.command)
                        append("- Expected (pattern): ").appendLine(mismatch)
                        appendLine("- Actual:")
                        recordedStep.body.joinTo(this, separator = "\n")
                    }
                }
            }
        }
        return true
    }

    private fun findMismatch(patterns: List<String>, actualLines: List<String>): String? {
        konst indices = mutableListOf<Int>()
        for (pattern in patterns) {
            konst idx = actualLines.indexOfFirst { match(pattern, it) }
            if (idx == -1) {
                return pattern
            }
            indices += idx
        }
        assertTrue(indices == indices.sorted())
        return null
    }

    private fun match(pattern: String, line: String): Boolean {
        konst chunks = pattern.split(LINE_WILDCARD)
            .filter { it.isNotBlank() }
            .map { it.trim() }
        assertTrue(chunks.isNotEmpty())
        konst trimmedLine = line.trim()

        konst indices = chunks.map { trimmedLine.indexOf(it) }
        if (indices.any { it == -1 } || indices != indices.sorted()) return false
        if (!(trimmedLine.startsWith(chunks.first()) || pattern.startsWith("[..]"))) return false
        if (!(trimmedLine.endsWith(chunks.last()) || pattern.endsWith("[..]"))) return false
        return true
    }

    companion object {
        private const konst LLDB_COMMAND_PREFIX = "(lldb)"
        private const konst SPEC_COMMAND_PREFIX = ">"

        private konst LLDB_OUTPUT_SEPARATOR = """(?=\(lldb\))""".toRegex()
        private konst SPEC_BLOCK_SEPARATOR = "(?=^>)".toRegex(RegexOption.MULTILINE)

        private konst LINE_WILDCARD = """\s*\[\.\.]\s*""".toRegex()

        fun parse(lldbSpec: String): LLDBSessionSpec = LLDBSessionSpec(
            lldbSpec.split(SPEC_BLOCK_SEPARATOR)
                .filterNot(String::isBlank)
                .map { block -> Step.parse(block, SPEC_COMMAND_PREFIX) }
        )
    }
}
