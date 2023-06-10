// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// WITH_COROUTINES

// MODULE: lib
// FILE: A.kt

package a

context(String)
suspend fun f() = this@String

// MODULE: main(lib)
// FILE: B.kt

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun box(): String {
    var result: String = "fail"
    konst block: suspend () -> String = {
        with("OK") { a.f() }
    }
    block.startCoroutine(handleResultContinuation { konstue ->
        result = konstue
    })
    return result
}
