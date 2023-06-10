// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KProperty1

class A<T>(konst t: T) {
    konst foo: T = t
}

fun bar() {
    konst x = A<String>::foo
    checkSubtype<KProperty1<A<String>, String>>(x)
    checkSubtype<KProperty1<A<String>, Any?>>(x)

    konst y = A<*>::foo
    checkSubtype<KProperty1<A<*>, Any?>>(y)
}
