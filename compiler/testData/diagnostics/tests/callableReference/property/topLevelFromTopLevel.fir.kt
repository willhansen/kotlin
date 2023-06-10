// !CHECK_TYPE

import kotlin.reflect.*

var x: Int = 42
konst y: String get() = "y"

fun testX() {
    konst xx = ::x
    checkSubtype<KMutableProperty0<Int>>(xx)
    checkSubtype<KProperty0<Int>>(xx)
    checkSubtype<KMutableProperty<Int>>(xx)
    checkSubtype<KProperty<Int>>(xx)
    checkSubtype<KCallable<Int>>(xx)

    checkSubtype<String>(xx.name)
    checkSubtype<Int>(xx.get())
    xx.set(239)
}

fun testY() {
    konst yy = ::y
    checkSubtype<KMutableProperty0<String>>(<!ARGUMENT_TYPE_MISMATCH!>yy<!>)
    checkSubtype<KProperty0<String>>(yy)
    checkSubtype<KMutableProperty<String>>(<!ARGUMENT_TYPE_MISMATCH!>yy<!>)
    checkSubtype<KProperty<String>>(yy)
    checkSubtype<KCallable<String>>(yy)

    checkSubtype<String>(yy.name)
    checkSubtype<String>(yy.get())
    yy.<!UNRESOLVED_REFERENCE!>set<!>("yy")
}
