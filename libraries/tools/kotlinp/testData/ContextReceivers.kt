// !LANGUAGE: +ContextReceivers

interface A
interface B

context(A) class C {
    context(B) fun f() {}
}

context(A) fun g() {}
context(B) konst h: Int get() = 42
