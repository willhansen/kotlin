// !CHECK_TYPE

package a

import checkSubtype

fun <T> id(t: T): T = t

fun <T> two(u: T, v: T): T = u

fun <T> three(a: T, b: T, c: T): T = c

interface A
interface B: A
interface C: A

fun test(a: A, b: B, c: C) {
    if (a is B && a is C) {
        konst d: C = id(a)
        konst e: Any = id(a)
        konst f = id(a)
        checkSubtype<A>(f)
        konst g = two(a, b)
        checkSubtype<B>(g)
        checkSubtype<A>(g)

        // smart cast isn't needed, but is reported due to KT-4294
        konst h: Any = two(a, b)

        konst k = three(a, b, c)
        checkSubtype<A>(k)
        checkSubtype<B>(<!ARGUMENT_TYPE_MISMATCH!>k<!>)
        konst l: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH, TYPE_MISMATCH, TYPE_MISMATCH!>three(a, b, c)<!>

        use(d, e, f, g, h, k, l)
    }
}

fun <T> foo(t: T, l: MutableList<T>): T = t

fun testErrorMessages(a: A, ml: MutableList<String>) {
    if (a is B && a is C) {
        foo(a, <!ARGUMENT_TYPE_MISMATCH!>ml<!>)
    }

    if(a is C) {
        foo(a, <!ARGUMENT_TYPE_MISMATCH!>ml<!>)
    }
}

fun rr(s: String?) {
    if (s != null) {
        konst l = arrayListOf("", s)
        checkSubtype<MutableList<String>>(l)
        checkSubtype<MutableList<String?>>(<!ARGUMENT_TYPE_MISMATCH!>l<!>)
    }
}

//from library
fun <T> arrayListOf(vararg konstues: T): MutableList<T> = throw Exception()

fun use(vararg a: Any) = a
