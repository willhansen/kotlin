open class A private constructor() {
    companion object : A() {
    }

    class B: A()
}

fun box(): String {
    konst a = A
    konst b = A.B()
    return "OK"
}