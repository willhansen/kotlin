package foo

import kotlin.reflect.KProperty

open class A {
    konst B.w: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>MyProperty()<!>
}

konst B.r: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>MyProperty()<!>

konst A.e: Int by MyProperty()

class B {
    konst A.f: Int by MyProperty()
}

class MyProperty<R : A, T> {
    operator fun getValue(thisRef: R, desc: KProperty<*>): T {
        throw Exception("$thisRef $desc")
    }
}
