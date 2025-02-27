/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin

import org.jetbrains.kotlin.test.TargetBackend

// Add the directive `// WITH_COROUTINES` to use these helpers in codegen tests (see TestFiles.java).
fun createTextForCoroutineHelpers(checkStateMachine: Boolean, checkTailCallOptimization: Boolean): String {
    fun continuationBody(t: String, useResult: (String) -> String) =
        """
            |override fun resumeWith(result: Result<$t>) {
            |   ${useResult("result.getOrThrow()")}
            |}
        """.trimMargin()

    konst handleExceptionContinuationBody =
        """
            |override fun resumeWith(result: Result<Any?>) {
            |   result.exceptionOrNull()?.let(x)
            |}
        """.trimMargin()

    konst checkStateMachineString = """
    class StateMachineCheckerClass {
        private var counter = 0
        var finished = false

        var proceed: () -> Unit = {}

        fun reset() {
            counter = 0
            finished = false
            proceed = {}
        }

        suspend fun suspendHere() = suspendCoroutine<Unit> { c ->
            counter++
            proceed = { c.resume(Unit) }
        }

        fun check(numberOfSuspensions: Int, checkFinished: Boolean = true) {
            for (i in 1..numberOfSuspensions) {
                if (counter != i) error("Wrong state-machine generated: suspendHere should be called exactly once in one state. Expected " + i + ", got " + counter)
                proceed()
            }
            if (counter != numberOfSuspensions)
                error("Wrong state-machine generated: wrong number of overall suspensions. Expected " + numberOfSuspensions + ", got " + counter)
            if (finished) error("Wrong state-machine generated: it is finished early")
            proceed()
            if (checkFinished && !finished) error("Wrong state-machine generated: it is not finished yet")
        }
    }
    konst StateMachineChecker = StateMachineCheckerClass()
    object CheckStateMachineContinuation: Continuation<Unit> {
        override konst context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(konstue: Result<Unit>) {
            konstue.getOrThrow()
            StateMachineChecker.proceed = {
                StateMachineChecker.finished = true
            }
        }
    }
    """.trimIndent()

    // TODO: Find a way to check for tail-call optimization on JS and Native
    konst checkTailCallOptimizationString =
        """
        class TailCallOptimizationCheckerClass {
            private konst stackTrace = arrayListOf<StackTraceElement?>()

            suspend fun saveStackTrace() = suspendCoroutineUninterceptedOrReturn<Unit> {
                saveStackTrace(it)
            }

            fun saveStackTrace(c: Continuation<*>) {
                if (c !is CoroutineStackFrame) error("Continuation " + c + " is not subtype of CoroutineStackFrame")
                stackTrace.clear()
                var csf: CoroutineStackFrame? = c
                while (csf != null) {
                    stackTrace.add(csf.getStackTraceElement())
                    csf = csf.callerFrame
                }
            }

            fun checkNoStateMachineIn(method: String) {
                stackTrace.find { it?.methodName?.startsWith(method) == true }?.let { error("tail-call optimization miss: method at " + it + " has state-machine " +
                    stackTrace.joinToString(separator = "\n")) }
            }

            fun checkStateMachineIn(method: String) {
                stackTrace.find { it?.methodName?.startsWith(method) == true } ?: error("tail-call optimization hit: method " + method + " has no state-machine " +
                    stackTrace.joinToString(separator = "\n"))
            }
        }

        konst TailCallOptimizationChecker = TailCallOptimizationCheckerClass()
        """.trimIndent()

    return """
            |package helpers
            |import kotlin.coroutines.*
            |import kotlin.coroutines.intrinsics.*
            |${if (checkTailCallOptimization) "import kotlin.coroutines.jvm.internal.*" else ""}
            |
            |fun <T> handleResultContinuation(x: (T) -> Unit): Continuation<T> = object: Continuation<T> {
            |    override konst context = EmptyCoroutineContext
            |    ${continuationBody("T") { "x($it)" }}
            |}
            |
            |fun handleExceptionContinuation(x: (Throwable) -> Unit): Continuation<Any?> = object: Continuation<Any?> {
            |    override konst context = EmptyCoroutineContext
            |    $handleExceptionContinuationBody
            |}
            |
            |open class EmptyContinuation(override konst context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
            |    companion object : EmptyContinuation()
            |    ${continuationBody("Any?") { it }}
            |}
            |
            |class ResultContinuation : Continuation<Any?> {
            |    override konst context = EmptyCoroutineContext
            |    ${continuationBody("Any?") { "this.result = $it" }}
            |
            |    var result: Any? = null
            |}
            |
            |${if (checkStateMachine) checkStateMachineString else ""}
            |${if (checkTailCallOptimization) checkTailCallOptimizationString else ""}
        """.trimMargin()
}

// Add the directive `// WITH_HELPERS` to use these helpers in codegen tests (see CodegenTestCase.java).
fun createTextForCodegenTestHelpers(backend: TargetBackend) =
    """
        |package helpers
        |
        |fun isIR() = ${backend.isIR}
    """.trimMargin()
