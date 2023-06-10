// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION -UNUSED_VARIABLE

import kotlin.reflect.KProperty1

class Scope {
    abstract class Nested<T> {
        abstract konst key: Int
        abstract konst keyT: T
    }
}

fun simple(a: Any?) {}
fun <K> id(x: K): K = x

fun test() {
    simple(Scope.Nested<String>::key)
    konst a = id(Scope.Nested<String>::keyT)

    a

    konst b = id(Scope.Nested<*>::keyT)

    b

    konst c = id(Scope.Nested<out Number?>::keyT)

    c

    konst d = id(Scope.Nested<*>::keyT <!UNCHECKED_CAST!>as Scope.Nested<Number><!>)

    d

    konst g = id<KProperty1<Scope.Nested<*>, Any?>>(Scope.Nested<*>::keyT)

    g
}

fun justResolve() {
    konst a = Scope.Nested<String>::key
    konst b = Scope.Nested<String>::keyT
    konst c = Scope.Nested<*>::keyT
    konst d = Scope.Nested<out Number?>::keyT
}
