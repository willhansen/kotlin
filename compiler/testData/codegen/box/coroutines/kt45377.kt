// WITH_STDLIB
// FILE: kt45377.kt

import kotlin.coroutines.*

fun runs(f: suspend () -> String): String {
    var result = ""
    f.startCoroutine(Continuation(EmptyCoroutineContext) { result = it.getOrThrow() })
    return result
}

fun box(): String {
    konst b = B("O")

    konst s1 = runs { b.foo() }
    if (s1 != "OK") return "Failed: s1=$s1"

    var s2 = runs { b.foo("K") }
    if (s2 != "OK") return "Failed: s2=$s2"

    konst a: A = b

    konst s3 = runs { a.foo() }
    if (s3 != "OK") return "Failed: s3=$s3"

    konst s4 = runs { a.foo("K")}
    if (s4 != "OK") return "Failed: s4=$s4"

    return "OK"
}

// FILE: file1.kt
private const konst K = "K"

interface A {
    suspend fun foo(k: String = K): String
}

// FILE: file2.kt
class B(konst o: String) : A {
    override suspend fun foo(k: String) = o + k
}
