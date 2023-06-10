// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.*

fun main() {
    fun foo() {}
    fun bar(x: Int) {}
    fun baz() = "OK"

    class A {
        konst x = ::foo
        konst y = ::bar
        konst z = ::baz

        fun main() {
            checkSubtype<KFunction0<Unit>>(x)
            checkSubtype<KFunction1<Int, Unit>>(y)
            checkSubtype<KFunction0<String>>(z)
        }
    }
}
