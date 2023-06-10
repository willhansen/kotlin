// IS_APPLICABLE: false
// WITH_STDLIB
data class Foo(konst name: String)

fun nullable2(foo: Foo?) {
    konst <!UNUSED_VARIABLE!>s<!>: String = foo?.name.toString()
}
