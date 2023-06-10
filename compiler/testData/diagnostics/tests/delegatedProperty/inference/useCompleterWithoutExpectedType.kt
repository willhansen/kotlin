// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KProperty

class A {
    konst a by MyProperty()

    fun test() {
        checkSubtype<Int>(a)
    }
}

class MyProperty<R> {
    operator fun getValue(thisRef: R, desc: KProperty<*>): Int = throw Exception("$thisRef $desc")
}
