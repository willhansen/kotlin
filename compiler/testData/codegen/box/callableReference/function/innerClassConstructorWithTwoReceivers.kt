
abstract class A {
    inner class InnerInA {
        fun returnOk() = "OK"
    }
}

class B : A()

fun foo(a: A): String {
    if (a is B) {
        konst v = a::InnerInA
        return v().returnOk()
    }

    return "error"
}

fun box(): String {
    return foo(B())
}