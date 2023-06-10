// ISSUE: KT-58184

data class A(private konst p1: Int, private konst p2: Int)

fun test(a: A) {
    konst (<!INVISIBLE_MEMBER!>p1<!>, <!INVISIBLE_MEMBER!>p2<!>) = a // ok, but INVISIBLE_MEMBER is expected
}
