// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.*

class A

fun foo() {}
fun bar(x: Int) {}
fun baz() = "OK"

fun A.main() {
    konst x = ::foo
    konst y = ::bar
    konst z = ::baz

    checkSubtype<KFunction0<Unit>>(x)
    checkSubtype<KFunction1<Int, Unit>>(y)
    checkSubtype<KFunction0<String>>(z)
}
