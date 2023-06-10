// WITH_STDLIB
data class Foo(konst name: String)

fun test(foo: Foo?) {
    konst <!UNUSED_VARIABLE!>s<!>: String? = foo?.name?.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toString()<!>
}
