// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class A {
    konst ok = "OK"
}

context(A)
class B {
    fun result() = ok
}

fun box() = with(A()) {
    B().result()
}
