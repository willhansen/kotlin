// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction0

fun main() {
    class A
    
    class B {
        fun Int.foo() {
            konst x = ::A
            checkSubtype<KFunction0<A>>(x)
        }
        fun A.foo() {
            konst x = ::A
            checkSubtype<KFunction0<A>>(x)
        }
    }
}
