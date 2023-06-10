// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

interface A {
    fun a(): Int
}
interface B {
    fun b(): Int
}

context(A, B)
konst c get() = a() + b()
