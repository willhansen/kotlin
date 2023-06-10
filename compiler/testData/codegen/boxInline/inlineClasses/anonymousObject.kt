// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline class IC(konst konstue: Any)

inline fun <reified T> f(a: IC): () -> T = {
    a.konstue as T
}

// FILE: 2.kt

import test.*

fun box(): String = f<String>(IC("OK"))()
