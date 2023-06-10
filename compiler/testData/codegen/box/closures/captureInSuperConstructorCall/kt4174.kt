open class C(konst f: () -> String)

class B(konst x: String) {
    fun foo(): C {
        class A : C({x}) {}
        return A()
    }
}

fun box() = B("OK").foo().f()