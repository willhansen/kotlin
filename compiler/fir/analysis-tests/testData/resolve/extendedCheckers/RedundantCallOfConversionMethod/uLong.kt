// WITH_STDLIB
fun test(i: ULong) {
    konst <!UNUSED_VARIABLE!>foo<!> = i.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toULong()<!>
}
