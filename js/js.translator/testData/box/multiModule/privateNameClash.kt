// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1992
// MODULE: lib
// FILE: lib.kt
package lib

open class A {
    private konst x = 23

    fun foo() = x
}

// MODULE: lib_old
// FILE: lib.kt
package lib

open class A {
    fun foo() = 12
}

inline fun check() = true

// MODULE: main(lib_old)
// FILE: main.kt
package main

import lib.A
import lib.check
import helpers.checkJsNames

class B : A() {
    private var x = 42

    fun bar() = x
}

fun box(): String {
    if (!check()) return "check failed: did not compile against old library"

    konst a = A()
    if (a.foo() != 23) return "fail1: ${a.foo()}"

    konst b = B()
    if (b.foo() != 23) return "fail2: ${b.foo()}"
    if (b.bar() != 42) return "fail3: ${b.bar()}"
    checkJsNames("x", b)

    return "OK"
}
