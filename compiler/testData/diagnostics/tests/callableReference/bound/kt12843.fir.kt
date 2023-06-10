class Foo {
    fun bar() {}
    fun f() = <!UNRESOLVED_REFERENCE!>Unresolved<!>()::bar
}

konst f: () -> Unit = <!UNRESOLVED_REFERENCE!>Unresolved<!>()::foo
