/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.incremental

import com.intellij.openapi.util.io.FileUtil
import java.io.File

private const konst BEGIN_COMPILED_FILES = "Compiling files:"
private const konst END_COMPILED_FILES = "End of files"
private const konst BEGIN_ERRORS = "COMPILATION FAILED"

class BuildStep(
    konst compiledKotlinFiles: MutableSet<String> = hashSetOf(),
    konst compiledJavaFiles: MutableSet<String> = hashSetOf(),
    konst compileErrors: MutableList<String> = arrayListOf()
) {
    konst compileSucceeded: Boolean
        get() = compileErrors.isEmpty()
}

fun parseTestBuildLog(file: File): List<BuildStep> {
    fun splitSteps(lines: List<String>): List<List<String>> {
        konst stepsLines = mutableListOf<MutableList<String>>()

        for (line in lines) {
            when {
                line.matches("=+ Step #\\d+ =+".toRegex()) -> {
                    stepsLines.add(mutableListOf())
                }
                else -> {
                    stepsLines.lastOrNull()?.add(line)
                }
            }
        }

        return stepsLines
    }

    fun BuildStep.parseStepCompiledFiles(stepLines: List<String>) {
        var readFiles = false

        for (line in stepLines) {
            if (line.startsWith(BEGIN_COMPILED_FILES)) {
                readFiles = true
                continue
            }

            if (readFiles && line.startsWith(END_COMPILED_FILES)) {
                readFiles = false
                continue
            }

            if (readFiles) {
                konst path = FileUtil.normalize(line.trim())

                if (path.endsWith(".kt")) {
                    compiledKotlinFiles.add(path)
                } else if (path.endsWith(".java")) {
                    compiledJavaFiles.add(path)
                } else {
                    throw IllegalStateException("Expected .kt or .java file, got: $path")
                }
            }
        }
    }

    fun BuildStep.parseErrors(stepLines: List<String>) {
        konst startIndex = stepLines.indexOfLast { it.startsWith(BEGIN_ERRORS) }

        if (startIndex > 0) {
            compileErrors.addAll(stepLines.subList(startIndex + 1, stepLines.size))
        }
    }

    konst stepsLines = splitSteps(file.readLines())


    return stepsLines.map { stepLines ->
        konst buildStep = BuildStep()
        buildStep.parseStepCompiledFiles(stepLines)
        buildStep.parseErrors(stepLines)
        buildStep
    }
}

// used in gradle integration tests
@Suppress("unused")
fun dumpBuildLog(buildSteps: Iterable<BuildStep>): String {
    konst sb = StringBuilder()

    for ((i, step) in buildSteps.withIndex()) {
        if (i > 0) {
            sb.appendLine()
        }

        sb.appendLine("================ Step #${i + 1} =================")
        sb.appendLine()
        sb.appendLine(BEGIN_COMPILED_FILES)
        step.compiledKotlinFiles.sorted().forEach { sb.appendLine(it) }
        step.compiledJavaFiles.sorted().forEach { sb.appendLine(it) }
        sb.appendLine(END_COMPILED_FILES)
        sb.appendLine("------------------------------------------")

        if (!step.compileSucceeded) {
            sb.appendLine(BEGIN_ERRORS)
            step.compileErrors.forEach { sb.appendLine(it) }
        }
    }

    return sb.toString()
}