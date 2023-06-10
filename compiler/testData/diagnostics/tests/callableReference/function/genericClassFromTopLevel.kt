// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction1

class A<T>(konst t: T) {
    fun foo(): T = t
}

fun bar() {
    konst x = A<String>::foo

    checkSubtype<KFunction1<A<String>, String>>(x)
}
