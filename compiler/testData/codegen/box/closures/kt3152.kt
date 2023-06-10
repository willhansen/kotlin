public class Test {
    konst content = 1
    inner class A {
        konst v = object {
            fun f() = content
        }
    }
}

fun box(): String {
    Test().A()

    return "OK"
}