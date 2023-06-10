// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class A {
    konst o = "O"
}
class B {
    konst k = "K"
}

context(B) fun A.f(a: Any, b: Any) = o + k

fun B.g(a: A): String {
    with (a) {
        return f(1, "2")
    }
}

fun box(): String {
    return B().g(A())
}
