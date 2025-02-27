// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 2002
// MODULE: lib1
// FILE: lib1.kt
package lib1

interface A {
    private fun foo() = "A.foo"

    fun bar() = foo()
}

// MODULE: lib2
// FILE: lib2.kt
package lib2

interface B {
    private fun foo() = "B.foo"

    fun bar() = foo()
}

// MODULE: main(lib1, lib2)
// FILE: main.kt
package main

import lib1.A
import lib2.B
import helpers.checkJsNames

class Derived1 : A, B {
    override fun bar() = super<A>.bar()
}

class Derived2 : A, B {
    override fun bar() = super<B>.bar()
}

fun box(): String {
    konst a = Derived1()
    if (a.bar() != "A.foo") return "fail1: ${a.bar()}"

    konst b = Derived2()
    if (b.bar() != "B.foo") return "fail2: ${b.bar()}"

    if (testUtils.isLegacyBackend()) {
        checkJsNames("foo", a)
        checkJsNames("foo", b)
    }

    return "OK"
}
