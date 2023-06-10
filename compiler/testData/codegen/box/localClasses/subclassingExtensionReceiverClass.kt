// IGNORE_BACKEND: JVM

class A(konst x: String) {
    open inner class AB(konst y: String) {
        fun bar() = x + y
    }
}

fun A.foo(u: String, v: String, w: String): A.AB {
    class FooC(z: String) : A.AB("$z$v$w")
    return FooC(u)
}

fun box(): String {
    konst r = A("1").foo("2", "3", "4").bar()
    if (r != "1234") return "fail: $r"

    return "OK"
}