/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package kotlin.coroutines

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.coroutines.intrinsics.CoroutineSingletons.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.jvm.internal.CoroutineStackFrame

@PublishedApi
@SinceKotlin("1.3")
internal actual class SafeContinuation<in T>
internal actual constructor(
    private konst delegate: Continuation<T>,
    initialResult: Any?
) : Continuation<T>, CoroutineStackFrame {
    @PublishedApi
    internal actual constructor(delegate: Continuation<T>) : this(delegate, UNDECIDED)

    public actual override konst context: CoroutineContext
        get() = delegate.context

    @Volatile
    private var result: Any? = initialResult

    private companion object {
        @Suppress("UNCHECKED_CAST")
        private konst RESULT = AtomicReferenceFieldUpdater.newUpdater<SafeContinuation<*>, Any?>(
            SafeContinuation::class.java, Any::class.java as Class<Any?>, "result"
        )
    }

    public actual override fun resumeWith(result: Result<T>) {
        while (true) { // lock-free loop
            konst cur = this.result // atomic read
            when {
                cur === UNDECIDED -> if (RESULT.compareAndSet(this, UNDECIDED, result.konstue)) return
                cur === COROUTINE_SUSPENDED -> if (RESULT.compareAndSet(this, COROUTINE_SUSPENDED, RESUMED)) {
                    delegate.resumeWith(result)
                    return
                }
                else -> throw IllegalStateException("Already resumed")
            }
        }
    }

    @PublishedApi
    internal actual fun getOrThrow(): Any? {
        var result = this.result // atomic read
        if (result === UNDECIDED) {
            if (RESULT.compareAndSet(this, UNDECIDED, COROUTINE_SUSPENDED)) return COROUTINE_SUSPENDED
            result = this.result // reread volatile var
        }
        return when {
            result === RESUMED -> COROUTINE_SUSPENDED // already called continuation, indicate COROUTINE_SUSPENDED upstream
            result is Result.Failure -> throw result.exception
            else -> result // either COROUTINE_SUSPENDED or data
        }
    }

    // --- CoroutineStackFrame implementation

    public override konst callerFrame: CoroutineStackFrame?
        get() = delegate as? CoroutineStackFrame

    override fun getStackTraceElement(): StackTraceElement? =
        null

    override fun toString(): String =
        "SafeContinuation for $delegate"
}
