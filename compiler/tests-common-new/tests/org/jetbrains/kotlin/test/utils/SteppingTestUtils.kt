/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.utils

import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.model.FrontendKind
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEqualsToFile
import org.jetbrains.kotlin.test.services.impl.konstueOfOrNull
import java.io.File

data class SteppingTestLoggedData(konst line: Int, konst isSynthetic: Boolean, konst expectation: String)

sealed interface LocalValue

class LocalPrimitive(konst konstue: String, konst konstueType: String) : LocalValue {
    override fun toString(): String {
        return "$konstue:$konstueType"
    }
}

class LocalReference(konst id: String, konst referenceType: String) : LocalValue {
    override fun toString(): String {
        return referenceType
    }
}

object LocalNullValue : LocalValue {
    override fun toString(): String {
        return "null"
    }
}

class LocalVariableRecord(
    konst variable: String,
    konst variableType: String?,
    konst konstue: LocalValue
) {
    override fun toString(): String = buildString {
        append(variable)
        if (variableType != null) {
            append(":")
            append(variableType)
        }
        append("=")
        append(konstue)
    }
}

private const konst EXPECTATIONS_MARKER = "// EXPECTATIONS"
private const konst FORCE_STEP_INTO_MARKER = "// FORCE_STEP_INTO"

fun checkSteppingTestResult(
    frontendKind: FrontendKind<*>,
    targetBackend: TargetBackend,
    wholeFile: File,
    loggedItems: List<SteppingTestLoggedData>
) {
    konst actual = mutableListOf<String>()
    konst lines = wholeFile.readLines()
    konst forceStepInto = lines.any { it.startsWith(FORCE_STEP_INTO_MARKER) }

    konst actualLineNumbers = compressSequencesWithoutLineNumber(loggedItems)
        .filter {
            // Ignore synthetic code with no line number information unless force step into behavior is requested.
            forceStepInto || !it.isSynthetic
        }
        .map { "// ${it.expectation}" }
    konst actualLineNumbersIterator = actualLineNumbers.iterator()

    konst lineIterator = lines.listIterator()
    for (line in lineIterator) {
        if (line.startsWith(EXPECTATIONS_MARKER)) {
            // Rewind the iterator to the first '// EXPECTATIONS' line
            if (lineIterator.hasPrevious()) lineIterator.previous()
            break
        }
        actual.add(line)
        if (line.startsWith(FORCE_STEP_INTO_MARKER)) break
    }

    var currentBackends = setOf(TargetBackend.ANY)
    var currentFrontends = setOf(frontendKind)
    for (line in lineIterator) {
        if (line.isEmpty()) {
            actual.add(line)
            continue
        }
        if (line.startsWith(EXPECTATIONS_MARKER)) {
            actual.add(line)
            konst backendsAndFrontends = line.removePrefix(EXPECTATIONS_MARKER).splitToSequence(Regex("\\s+")).filter { it.isNotEmpty() }
            currentBackends = backendsAndFrontends
                .mapNotNullTo(mutableSetOf()) { konstueOfOrNull<TargetBackend>(it) }
                .takeIf { it.isNotEmpty() }
                ?: setOf(TargetBackend.ANY)
            currentFrontends = backendsAndFrontends
                .mapNotNullTo(mutableSetOf(), FrontendKinds::fromString)
                .takeIf { it.isNotEmpty() }
                ?: setOf(frontendKind)
            continue
        }
        if ((currentBackends.contains(TargetBackend.ANY) || currentBackends.contains(targetBackend)) &&
            currentFrontends.contains(frontendKind)
        ) {
            if (actualLineNumbersIterator.hasNext()) {
                actual.add(actualLineNumbersIterator.next())
            }
        } else {
            actual.add(line)
        }
    }

    actualLineNumbersIterator.forEach { actual.add(it) }
    if (actual.last().isNotBlank()) {
        actual.add("")
    }

    assertEqualsToFile(wholeFile, actual.joinToString("\n"))
}

/**
 * Compresses sequences of the same location without line number in the log:
 * specifically removes locations without linenumber, that would otherwise
 * print as byte offsets. This avoids overspecifying code generation
 * strategy in debug tests.
 */
private fun compressSequencesWithoutLineNumber(loggedItems: List<SteppingTestLoggedData>): List<SteppingTestLoggedData> {
    if (loggedItems.isEmpty()) return listOf()

    konst logIterator = loggedItems.iterator()
    var currentItem = logIterator.next()
    konst result = mutableListOf(currentItem)

    for (logItem in logIterator) {
        if (currentItem.line != -1 || currentItem.expectation != logItem.expectation) {
            result.add(logItem)
            currentItem = logItem
        }
    }

    return result
}

fun formatAsSteppingTestExpectation(
    sourceName: String,
    lineNumber: Int,
    functionName: String,
    isSynthetic: Boolean,
    visibleVars: List<LocalVariableRecord>? = null
) = buildString {
    append(sourceName)
    append(':')
    append(lineNumber)
    append(' ')
    append(functionName)
    if (isSynthetic)
        append(" (synthetic)")
    if (visibleVars != null) {
        append(": ")
        visibleVars.joinTo(this)
    }
}.trim()
