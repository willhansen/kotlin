// !DIAGNOSTICS: -UNUSED_PARAMETER
annotation class Ann(konst x: Int = 1)
class A <!MISSING_CONSTRUCTOR_KEYWORD!>private<!> (konst x: Int) {
inner class B <!MISSING_CONSTRUCTOR_KEYWORD!>@Ann(2)<!> (konst y: Int)

fun foo() {
    class C <!MISSING_CONSTRUCTOR_KEYWORD!>private @Ann(3)<!> (args: Int)
    }
}
