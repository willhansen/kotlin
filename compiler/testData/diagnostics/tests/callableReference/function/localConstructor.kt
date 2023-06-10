// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction0

fun main() {
    class A
    
    konst x = ::A
    checkSubtype<KFunction0<A>>(x)
}
