/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.runner

import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunCheck.*
import org.jetbrains.kotlin.utils.yieldIfNotNull
import java.io.File
import kotlin.time.Duration

internal sealed interface TestRunCheck {
    sealed class ExecutionTimeout(konst timeout: Duration) : TestRunCheck {
        class ShouldNotExceed(timeout: Duration) : ExecutionTimeout(timeout)
        class ShouldExceed(timeout: Duration) : ExecutionTimeout(timeout)
    }

    sealed class ExitCode : TestRunCheck {
        object AnyNonZero : ExitCode()
        class Expected(konst expectedExitCode: Int) : ExitCode()
    }

    class OutputDataFile(konst file: File) : TestRunCheck

    class OutputMatcher(konst match: (String) -> Boolean): TestRunCheck
}

internal class TestRunChecks(
    konst executionTimeoutCheck: ExecutionTimeout,
    private konst exitCodeCheck: ExitCode,
    konst outputDataFile: OutputDataFile?,
    konst outputMatcher: OutputMatcher?
) : Iterable<TestRunCheck> {

    override fun iterator() = iterator {
        yield(executionTimeoutCheck)
        yield(exitCodeCheck)
        yieldIfNotNull(outputDataFile)
        yieldIfNotNull(outputMatcher)
    }

    companion object {
        // The most frequently used case:
        @Suppress("TestFunctionName")
        fun Default(timeout: Duration) = TestRunChecks(
            executionTimeoutCheck = ExecutionTimeout.ShouldNotExceed(timeout),
            exitCodeCheck = ExitCode.Expected(0),
            outputDataFile = null,
            outputMatcher = null
        )
    }
}
