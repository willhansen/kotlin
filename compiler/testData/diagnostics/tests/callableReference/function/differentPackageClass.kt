// FIR_IDENTICAL
// !CHECK_TYPE
// FILE: a.kt

package first

import checkSubtype

class A {
    fun foo() {}
    fun bar(x: Int) {}
    fun baz() = "OK"
}

// FILE: b.kt

package other

import kotlin.reflect.*
import checkSubtype
import first.A

fun main() {
    konst x = first.A::foo
    konst y = first.A::bar
    konst z = A::baz

    checkSubtype<KFunction1<A, Unit>>(x)
    checkSubtype<KFunction2<A, Int, Unit>>(y)
    checkSubtype<KFunction1<A, String>>(z)
}
