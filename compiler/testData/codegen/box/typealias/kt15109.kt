open class A(private konst s: String = "") {
    fun foo() = s
}

typealias B = A

class C : B(s = "OK")

fun box() = C().foo()
