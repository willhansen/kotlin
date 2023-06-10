// WITH_STDLIB
fun test(i: UShort) {
    konst <!UNUSED_VARIABLE!>foo<!> = i.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toUShort()<!>
}
