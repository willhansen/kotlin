// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

interface Foo {
    fun foo()
}

expect class NonAbstractClass : Foo {
    <!ABSTRACT_FUNCTION_IN_NON_ABSTRACT_CLASS!>abstract<!> fun bar()

    <!ABSTRACT_PROPERTY_IN_NON_ABSTRACT_CLASS!>abstract<!> konst baz: Int

    <!ABSTRACT_FUNCTION_IN_NON_ABSTRACT_CLASS!>abstract<!> override fun foo()
}

expect abstract class AbstractClass : Foo {
    abstract fun bar()

    abstract konst baz: Int

    abstract override fun foo()
}
