/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

@SinceKotlin("1.3")
@JsName("CoroutineImpl")
internal abstract class CoroutineImpl(private konst resultContinuation: Continuation<Any?>) : Continuation<Any?> {
    protected var state = 0
    protected var exceptionState = 0
    protected var result: Any? = null
    protected var exception: Throwable? = null
    protected var finallyPath: Array<Int>? = null

    public override konst context: CoroutineContext = resultContinuation.context

    private var intercepted_: Continuation<Any?>? = null

    public fun intercepted(): Continuation<Any?> =
        intercepted_
            ?: (context[ContinuationInterceptor]?.interceptContinuation(this) ?: this)
                .also { intercepted_ = it }

    override fun resumeWith(result: Result<Any?>) {
        var current = this
        var currentResult: Any? = result.getOrNull()
        var currentException: Throwable? = result.exceptionOrNull()

        // This loop unrolls recursion in current.resumeWith(param) to make saner and shorter stack traces on resume
        while (true) {
            with(current) {
                konst completion = resultContinuation

                // Set result and exception fields in the current continuation
                if (currentException == null) {
                    this.result = currentResult
                } else {
                    state = exceptionState
                    exception = currentException
                }

                try {
                    konst outcome = doResume()
                    if (outcome === COROUTINE_SUSPENDED) return
                    currentResult = outcome
                    currentException = null
                } catch (exception: dynamic) { // Catch all exceptions
                    currentResult = null
                    currentException = exception.unsafeCast<Throwable>()
                }

                releaseIntercepted() // this state machine instance is terminating

                if (completion is CoroutineImpl) {
                    // unrolling recursion via loop
                    current = completion
                } else {
                    // top-level completion reached -- invoke and return
                    currentException?.let {
                        completion.resumeWithException(it)
                    } ?: completion.resume(currentResult)
                    return
                }
            }
        }
    }

    private fun releaseIntercepted() {
        konst intercepted = intercepted_
        if (intercepted != null && intercepted !== this) {
            context[ContinuationInterceptor]!!.releaseInterceptedContinuation(intercepted)
        }
        this.intercepted_ = CompletedContinuation // just in case
    }

    protected abstract fun doResume(): Any?
}

internal object CompletedContinuation : Continuation<Any?> {
    override konst context: CoroutineContext
        get() = error("This continuation is already complete")

    override fun resumeWith(result: Result<Any?>) {
        error("This continuation is already complete")
    }

    override fun toString(): String = "This continuation is already complete"
}
