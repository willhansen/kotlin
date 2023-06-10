// !CHECK_TYPE
package a

import checkType
import _
import checkSubtype

fun <T> id(t: T): T = t

fun <T> either(t1: T, t2: T): T = t1

fun other(s: String) {}

fun <T> otherGeneric(l: List<T>) {}

fun test() {
    konst a: Byte = id(1)

    konst b: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>id(300)<!>

    konst c: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>id(9223372036854775807)<!>

    konst d = id(22)
    checkSubtype<Int>(d)

    konst e = id(9223372036854775807)
    e checkType { _<Long>() }

    konst f: Byte = either(1, 2)

    konst g: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>either(1, 300)<!>

    other(<!ARGUMENT_TYPE_MISMATCH!>11<!>)

    otherGeneric(<!ARGUMENT_TYPE_MISMATCH!>1<!>)

    konst r = either(1, "")
    r checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Any>() }

    use(a, b, c, d, e, f, g, r)
}

fun use(vararg a: Any?) = a

interface Inv<T>

fun <T> exactBound(t: T, l: Inv<T>): T = throw Exception("$t $l")

fun testExactBound(invS: Inv<String>, invI: Inv<Int>, invB: Inv<Byte>) {
    exactBound(1, <!ARGUMENT_TYPE_MISMATCH!>invS<!>)
    exactBound(1, invI)

    konst b = exactBound(1, invB)
    b checkType { _<Byte>() }
}

interface Cov<out T>

fun <T> lowerBound(t: T, l : Cov<T>): T = throw Exception("$t $l")

fun testLowerBound(cov: Cov<String>, covN: Cov<Number>) {
    konst r = lowerBound(1, cov)
    r checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Any>() }

    konst n = lowerBound(1, covN)
    n checkType { _<Number>() }
}

interface Contr<in T>

fun <T> upperBound(t: T, l: Contr<T>): T = throw Exception("$t $l")

fun testUpperBound(contrS: Contr<String>, contrB: Contr<Byte>, contrN: Contr<Number>) {
    upperBound(1, <!ARGUMENT_TYPE_MISMATCH!>contrS<!>)

    konst n = upperBound(1, contrN)
    n checkType { _<Int>() }

    konst b = upperBound(1, contrB)
    b checkType { _<Byte>() }
}
