// SCOPE_DUMP: C:foo;x, D:foo;x

interface A {
    fun foo(): Any
    konst x: Any
}

interface B {
    fun foo(): Any
    konst x: Any
}

interface C : A, B

interface D : C {
    override fun foo(): Any
    override konst x: Any
}
