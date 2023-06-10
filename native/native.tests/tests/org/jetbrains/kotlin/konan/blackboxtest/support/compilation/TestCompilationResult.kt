/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.compilation

import org.jetbrains.kotlin.konan.blackboxtest.support.LoggedData
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail

internal sealed interface TestCompilationResult<A : TestCompilationArtifact> {
    sealed interface ImmediateResult<A : TestCompilationArtifact> : TestCompilationResult<A> {
        konst loggedData: LoggedData
    }

    sealed interface Failure : ImmediateResult<Nothing>

    data class Success<A : TestCompilationArtifact>(konst resultingArtifact: A, override konst loggedData: LoggedData.CompilerCall) :
        ImmediateResult<A>

    data class CompilationToolFailure(override konst loggedData: LoggedData.CompilationToolCall) : Failure
    data class UnexpectedFailure(override konst loggedData: LoggedData) : Failure
    data class DependencyFailures(konst causes: Set<Failure>) : TestCompilationResult<Nothing>

    companion object {
        fun <A : TestCompilationArtifact> TestCompilationResult<A>.assertSuccess(): Success<A> = when (this) {
            is Success -> this
            is Failure -> fail { describeFailure() }
            is DependencyFailures -> fail { describeDependencyFailures() }
        }

        private fun Failure.describeFailure() = loggedData.withErrorMessage(
            when (this@describeFailure) {
                is CompilationToolFailure -> "Compilation failed."
                is UnexpectedFailure -> "Compilation failed with unexpected exception."
            }
        )

        private fun DependencyFailures.describeDependencyFailures() =
            buildString {
                appendLine("Compilation aborted due to errors in dependency compilations (${causes.size} items). See details below.")
                appendLine()
                causes.forEachIndexed { index, cause ->
                    append("#").append(index + 1).append(". ")
                    appendLine(cause.describeFailure())
                }
            }
    }
}
