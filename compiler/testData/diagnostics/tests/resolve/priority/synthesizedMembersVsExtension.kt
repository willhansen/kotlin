// !CHECK_TYPE

data class A(konst foo: Int)

operator fun A.<!EXTENSION_SHADOWED_BY_MEMBER!>component1<!>(): String = ""

fun test(a: A) {
    konst (b) = a
    b checkType { _<Int>() }
}