// !LANGUAGE: +ContextReceivers

interface A {
    fun a(): Int
}
interface B {
    fun b(): Int
}

context(A)
konst a = 1

context(A, B)
var b = 2

context(A, B)
konst c get() = a() + b()
