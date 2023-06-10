object Scope1 {
    konst someVar: Any = Any()

    fun foo() {
        <!FUNCTION_EXPECTED!>someVar<!>(1)
    }
}

object Scope2 {
    class Foo

    fun use() {
        konst foo = Foo()
        <!FUNCTION_EXPECTED!>foo<!>()
    }
}
