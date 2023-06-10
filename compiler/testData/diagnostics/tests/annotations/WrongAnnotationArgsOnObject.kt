package test

@BadAnnotation(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
object SomeObject

konst some = SomeObject

annotation class BadAnnotation(konst s: String)
