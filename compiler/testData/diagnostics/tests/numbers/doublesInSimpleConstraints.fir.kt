// !CHECK_TYPE

package a

import checkSubtype

fun <T> id(t: T): T = t

fun <T> either(t1: T, t2: T): T = t1

fun test() {
    konst a: Float = id(2.0.toFloat())

    konst b = id(2.0)
    checkSubtype<Double>(b)

    konst c = either<Number>(1, 2.3)
    checkSubtype<Number>(c)

    konst d = either(11, 2.3)
    checkSubtype<Any>(d)

    konst e: Float = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>id(1)<!>
}
