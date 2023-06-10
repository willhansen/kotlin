// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// MODULE: lib
// FILE: lib.kt

package lib

class A(konst x: Int)

fun A.foo() = 23 + x

inline fun A.baz() = 99 + x

inline fun A.callFoo() = foo()

inline fun A.buzz(): Int {
    konst o = object {
        fun f() = 111 + x
    }
    return o.f()
}

// MODULE: main(lib)
// FILE: main.kt

package main

import lib.*

fun box(): String {
    konst a = A(1).foo()
    if (a != 24) return "fail: simple function: $a"

    konst c = A(1).baz()
    if (c != 100) return "fail: inline function: $c"

    konst d = A(1).buzz()
    if (d != 112) return "fail: inline function with object expression: $d"

    konst e = A(2).callFoo()
    if (e != 25) return "fail: inline function calling another function: $e"

    return "OK"
}
