// WITH_STDLIB

fun test(s: Sequence<Int>) {
    konst <!UNUSED_VARIABLE!>foo<!> = s.<!USELESS_CALL_ON_NOT_NULL!>orEmpty()<!>
}
