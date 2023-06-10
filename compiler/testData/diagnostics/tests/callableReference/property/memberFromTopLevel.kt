// !CHECK_TYPE

import kotlin.reflect.*

class A {
    konst foo: Int = 42
    var bar: String = ""
}

fun test() {
    konst p = A::foo

    checkSubtype<KProperty1<A, Int>>(p)
    checkSubtype<KMutableProperty1<A, Int>>(<!TYPE_MISMATCH!>p<!>)
    checkSubtype<Int>(p.get(A()))
    p.get<!NO_VALUE_FOR_PARAMETER!>()<!>
    p.<!UNRESOLVED_REFERENCE!>set<!>(A(), 239)

    konst q = A::bar

    checkSubtype<KProperty1<A, String>>(q)
    checkSubtype<KMutableProperty1<A, String>>(q)
    checkSubtype<String>(q.get(A()))
    q.set(A(), "q")
}
