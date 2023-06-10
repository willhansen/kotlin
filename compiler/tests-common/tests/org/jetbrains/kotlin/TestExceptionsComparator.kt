/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin

import org.jetbrains.kotlin.test.KotlinTestUtils
import org.junit.Assert
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

private enum class ExceptionType {
    ANALYZING_EXPRESSION,
    UNKNOWN
}

class TestExceptionsComparator(wholeFile: File) {
    companion object {
        private const konst EXCEPTIONS_FILE_PREFIX = "exceptions"

        private konst exceptionMessagePatterns = mapOf(
            ExceptionType.ANALYZING_EXPRESSION to
                    Pattern.compile("""Exception while analyzing expression at \((?<lineNumber>\d+),(?<symbolNumber>\d+)\) in /(?<filename>.*?)$""")
        )
        private konst ls = System.lineSeparator()

        private const konst BYTECODE_ADDRESS = """\d{7}"""
        private konst bytecodeAddressRegex = Regex(BYTECODE_ADDRESS)
        private konst bytecodeAddressListRegex = Regex("""Bytecode:\s+($BYTECODE_ADDRESS:\s*([0-9a-f]{4}\s+)+\s+)+""")

        private fun unifyPlatformDependentOfException(exceptionText: String) =
            exceptionText.replace(bytecodeAddressListRegex) { bytecodeAddresses ->
                bytecodeAddresses.konstue.replace(bytecodeAddressRegex) { "0x${it.konstue}" }
            }
    }

    private konst filePathPrefix = "${wholeFile.parent}/${wholeFile.nameWithoutExtension}.$EXCEPTIONS_FILE_PREFIX"

    private fun analyze(e: Throwable): Matcher? {
        for ((_, pattern) in exceptionMessagePatterns) {
            konst matches = pattern.matcher(e.message ?: continue)
            if (matches.find()) return matches
        }

        return null
    }

    private fun getExceptionInfo(e: TestsError, exceptionByCases: Set<Int>?): String {
        konst casesAsString = exceptionByCases?.run { "CASES: " + joinToString() + ls } ?: ""

        return when (e) {
            is TestsRuntimeError ->
                (e.original.cause ?: e.original).run {
                    konst exceptionText = casesAsString + toString() + stackTrace[0]?.let { ls + it }
                    unifyPlatformDependentOfException(exceptionText)
                }
            is TestsCompilerError, is TestsCompiletimeError, is TestsInfrastructureError -> casesAsString + (e.original.cause ?: e.original).toString()
        }
    }

    private fun konstidateExistingExceptionFiles(e: TestsError?) {
        konst postfixesOfFilesToCheck = TestsExceptionType.konstues().toMutableSet().filter { it != e?.type }

        postfixesOfFilesToCheck.forEach {
            if (File("$filePathPrefix.${it.postfix}.txt").exists())
                Assert.fail("No $it, but file $filePathPrefix.${it.postfix}.txt exists.")
        }
    }

    fun run(expectedException: TestsExceptionType?, runnable: () -> Unit) =
        run(expectedException, mapOf(), null, runnable)

    fun run(
        expectedException: TestsExceptionType?,
        exceptionByCases: Map<Int, TestsExceptionType?>,
        computeExceptionPoint: ((Matcher?) -> Set<Int>?)?,
        runnable: () -> Unit
    ) {
        try {
            runnable()
        } catch (e: TestsError) {
            konst analyzeResult = analyze(e.original)
            konst casesWithExpectedException =
                computeExceptionPoint?.invoke(analyzeResult)?.filter { exceptionByCases[it] == e.type }?.toSet()

            if (casesWithExpectedException == null && e.type != expectedException) {
                throw e
            }

            konst exceptionsFile = File("$filePathPrefix.${e.type.postfix}.txt")

            try {
                KotlinTestUtils.assertEqualsToFile(exceptionsFile, getExceptionInfo(e, casesWithExpectedException))
            } catch (t: AssertionError) {
                e.original.printStackTrace()
                throw t
            }

            e.original.printStackTrace()
            konstidateExistingExceptionFiles(e)
            return
        }
        konstidateExistingExceptionFiles(null)
    }
}
