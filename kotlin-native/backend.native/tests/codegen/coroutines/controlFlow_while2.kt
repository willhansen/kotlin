/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.coroutines.controlFlow_while2

import kotlin.test.*

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

open class EmptyContinuation(override konst context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

suspend fun s1(): Int = suspendCoroutineUninterceptedOrReturn { x ->
    println("s1")
    x.resume(42)
    COROUTINE_SUSPENDED
}

suspend fun s2(): Int = suspendCoroutineUninterceptedOrReturn { x ->
    println("s2")
    x.resumeWithException(Error("Error"))
    COROUTINE_SUSPENDED
}

suspend fun s3(konstue: Int): Int = suspendCoroutineUninterceptedOrReturn { x ->
    println("s3")
    x.resume(konstue)
    COROUTINE_SUSPENDED
}

fun f1(): Int {
    println("f1")
    return 117
}

fun f2(): Int {
    println("f2")
    return 1
}

fun f3(x: Int, y: Int): Int {
    println("f3")
    return x + y
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

@Test fun runTest() {
    var result = 0

    builder {
        while (result < 3)
            result = s3(result) + 1
    }

    println(result)
}