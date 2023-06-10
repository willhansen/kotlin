// !CHECK_TYPE

import kotlin.reflect.KProperty1

interface Base {
    konst x: Any
}

class A : Base {
    override konst x: String = ""
}

open class B : Base {
    override konst x: Number = 1.0
}

class C : B() {
    override konst x: Int = 42
}

fun test() {
    konst base = Base::x
    checkSubtype<KProperty1<Base, Any>>(base)
    checkSubtype<Any>(base.get(A()))
    checkSubtype<Number>(<!TYPE_MISMATCH!>base.get(B())<!>)
    checkSubtype<Int>(<!TYPE_MISMATCH!>base.get(C())<!>)

    konst a = A::x
    checkSubtype<KProperty1<A, String>>(a)
    checkSubtype<String>(a.get(A()))
    checkSubtype<Number>(<!TYPE_MISMATCH!>a.get(<!TYPE_MISMATCH!>B()<!>)<!>)

    konst b = B::x
    checkSubtype<KProperty1<B, Number>>(b)
    checkSubtype<Int>(<!TYPE_MISMATCH!>b.get(C())<!>)
}
