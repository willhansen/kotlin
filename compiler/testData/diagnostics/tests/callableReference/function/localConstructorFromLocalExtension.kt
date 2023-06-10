// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction0

fun main() {
    class A
    
    fun A.foo() {
        konst x = ::A
        checkSubtype<KFunction0<A>>(x)
    }
    
    fun Int.foo() {
        konst x = ::A
        checkSubtype<KFunction0<A>>(x)
    }
}
