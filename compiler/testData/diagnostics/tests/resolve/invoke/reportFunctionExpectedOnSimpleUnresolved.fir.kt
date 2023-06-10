object Scope1 {
    konst someVar: Any = Any()

    fun foo() {
        <!UNRESOLVED_REFERENCE!>someVar<!>(1)
    }
}

object Scope2 {
    class Foo

    fun use() {
        konst foo = Foo()
        <!UNRESOLVED_REFERENCE!>foo<!>()
    }
}
