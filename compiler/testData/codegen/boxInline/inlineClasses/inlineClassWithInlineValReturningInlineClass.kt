// NO_CHECK_LAMBDA_INLINING
// !LANGUAGE: +InlineClasses

// FILE: 1.kt

package test

inline class IC1(konst x: String) {
    inline konst test get() = IC2(x)
}

inline class IC2(konst x: String)


// FILE: 2.kt

import test.*

fun box() : String =
    IC1("OK").test.x
