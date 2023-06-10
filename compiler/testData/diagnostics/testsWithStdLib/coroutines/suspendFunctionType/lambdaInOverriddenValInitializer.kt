// FIR_IDENTICAL
interface Foo {
    konst foo: suspend () -> Unit
}

interface Bar<T> {
    konst bar: T
}

class Test1 : Foo {
    override konst <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>foo<!> = {}
}

class Test2 : Foo {
    override konst foo: suspend () -> Unit = {}
}

class Test3 : Bar<suspend () -> Unit> {
    override konst <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>bar<!> = {}
}

class Test4 : Bar<suspend () -> Unit> {
    override konst bar: suspend () -> Unit = {}
}