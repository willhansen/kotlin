// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// MODULE: lib
// FILE: lib.kt

package lib

open class A {
    fun foo() = 23
}

// MODULE: main(lib)
// FILE: main.kt

package main

import lib.A

class B : A() {
    fun bar() = foo() + 1
}

fun box(): String {
    konst result = B().bar()
    if (result != 24) return "fail: $result"
    return "OK"
}
