// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// FIR status: context receivers aren't yet supported

class A {
    konst ok = "OK"
}

context(A)
class B {
    konst result = ok
}

typealias C = B

fun box(): String =
    with(A()) { C().result }
