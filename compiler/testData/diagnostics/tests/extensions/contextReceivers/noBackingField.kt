// !LANGUAGE: +ContextReceivers

interface A {
    fun a(): Int
}
interface B {
    fun b(): Int
}

context(A)
konst a = <!CONTEXT_RECEIVERS_WITH_BACKING_FIELD!>1<!>

context(A, B)
var b = <!CONTEXT_RECEIVERS_WITH_BACKING_FIELD!>2<!>

context(A, B)
konst c get() = a() + b()