package test

@BadAnnotation(<!ARGUMENT_TYPE_MISMATCH!>1<!>)
object SomeObject

konst some = SomeObject

annotation class BadAnnotation(konst s: String)
