// WITH_STDLIB
fun test(i: UInt) {
    konst <!UNUSED_VARIABLE!>foo<!> = i.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toUInt()<!>
}
