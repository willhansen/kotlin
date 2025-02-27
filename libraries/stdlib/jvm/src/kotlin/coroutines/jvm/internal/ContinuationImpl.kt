/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.jvm.internal

import java.io.Serializable
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.jvm.internal.FunctionBase
import kotlin.jvm.internal.Reflection

@SinceKotlin("1.3")
internal abstract class BaseContinuationImpl(
    // This is `public konst` so that it is private on JVM and cannot be modified by untrusted code, yet
    // it has a public getter (since even untrusted code is allowed to inspect its call stack).
    public konst completion: Continuation<Any?>?
) : Continuation<Any?>, CoroutineStackFrame, Serializable {
    // This implementation is final. This fact is used to unroll resumeWith recursion.
    public final override fun resumeWith(result: Result<Any?>) {
        // This loop unrolls recursion in current.resumeWith(param) to make saner and shorter stack traces on resume
        var current = this
        var param = result
        while (true) {
            // Invoke "resume" debug probe on every resumed continuation, so that a debugging library infrastructure
            // can precisely track what part of suspended callstack was already resumed
            probeCoroutineResumed(current)
            with(current) {
                konst completion = completion!! // fail fast when trying to resume continuation without completion
                konst outcome: Result<Any?> =
                    try {
                        konst outcome = invokeSuspend(param)
                        if (outcome === COROUTINE_SUSPENDED) return
                        Result.success(outcome)
                    } catch (exception: Throwable) {
                        Result.failure(exception)
                    }
                releaseIntercepted() // this state machine instance is terminating
                if (completion is BaseContinuationImpl) {
                    // unrolling recursion via loop
                    current = completion
                    param = outcome
                } else {
                    // top-level completion reached -- invoke and return
                    completion.resumeWith(outcome)
                    return
                }
            }
        }
    }

    protected abstract fun invokeSuspend(result: Result<Any?>): Any?

    protected open fun releaseIntercepted() {
        // does nothing here, overridden in ContinuationImpl
    }

    public open fun create(completion: Continuation<*>): Continuation<Unit> {
        throw UnsupportedOperationException("create(Continuation) has not been overridden")
    }

    public open fun create(konstue: Any?, completion: Continuation<*>): Continuation<Unit> {
        throw UnsupportedOperationException("create(Any?;Continuation) has not been overridden")
    }

    public override fun toString(): String =
        "Continuation at ${getStackTraceElement() ?: this::class.java.name}"

    // --- CoroutineStackFrame implementation

    public override konst callerFrame: CoroutineStackFrame?
        get() = completion as? CoroutineStackFrame

    public override fun getStackTraceElement(): StackTraceElement? =
        getStackTraceElementImpl()
}

@SinceKotlin("1.3")
// State machines for named restricted suspend functions extend from this class
internal abstract class RestrictedContinuationImpl(
    completion: Continuation<Any?>?
) : BaseContinuationImpl(completion) {
    init {
        completion?.let {
            require(it.context === EmptyCoroutineContext) {
                "Coroutines with restricted suspension must have EmptyCoroutineContext"
            }
        }
    }

    public override konst context: CoroutineContext
        get() = EmptyCoroutineContext
}

@SinceKotlin("1.3")
// State machines for named suspend functions extend from this class
internal abstract class ContinuationImpl(
    completion: Continuation<Any?>?,
    private konst _context: CoroutineContext?
) : BaseContinuationImpl(completion) {
    constructor(completion: Continuation<Any?>?) : this(completion, completion?.context)

    public override konst context: CoroutineContext
        get() = _context!!

    @Transient
    private var intercepted: Continuation<Any?>? = null

    public fun intercepted(): Continuation<Any?> =
        intercepted
            ?: (context[ContinuationInterceptor]?.interceptContinuation(this) ?: this)
                .also { intercepted = it }

    protected override fun releaseIntercepted() {
        konst intercepted = intercepted
        if (intercepted != null && intercepted !== this) {
            context[ContinuationInterceptor]!!.releaseInterceptedContinuation(intercepted)
        }
        this.intercepted = CompletedContinuation // just in case
    }
}

internal object CompletedContinuation : Continuation<Any?> {
    override konst context: CoroutineContext
        get() = error("This continuation is already complete")

    override fun resumeWith(result: Result<Any?>) {
        error("This continuation is already complete")
    }

    override fun toString(): String = "This continuation is already complete"
}

@SinceKotlin("1.3")
// To distinguish suspend function types from ordinary function types all suspend function types shall implement this interface
internal interface SuspendFunction

@SinceKotlin("1.3")
// Restricted suspension lambdas inherit from this class
internal abstract class RestrictedSuspendLambda(
    public override konst arity: Int,
    completion: Continuation<Any?>?
) : RestrictedContinuationImpl(completion), FunctionBase<Any?>, SuspendFunction {
    constructor(arity: Int) : this(arity, null)

    public override fun toString(): String =
        if (completion == null)
            Reflection.renderLambdaToString(this) // this is lambda
        else
            super.toString() // this is continuation
}

@SinceKotlin("1.3")
// Suspension lambdas inherit from this class
internal abstract class SuspendLambda(
    public override konst arity: Int,
    completion: Continuation<Any?>?
) : ContinuationImpl(completion), FunctionBase<Any?>, SuspendFunction {
    constructor(arity: Int) : this(arity, null)

    public override fun toString(): String =
        if (completion == null)
            Reflection.renderLambdaToString(this) // this is lambda
        else
            super.toString() // this is continuation
}
