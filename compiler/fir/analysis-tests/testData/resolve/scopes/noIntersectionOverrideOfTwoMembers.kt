// SCOPE_DUMP: C:foo;x, Explicit:foo;x, Implicit:foo;x

interface A {
    fun foo(): Any
    konst x: Any
}

interface B : A {
    override fun foo(): Any
    override konst x: Any
}

interface C : A, B

interface D {
    fun foo(): Int
    konst x: Any
}

interface Explicit : C, D {
    override fun foo(): Int
    override konst x: Any
}

interface Implicit : C, D
