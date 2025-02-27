abstract class Base {
    abstract fun foo(a: String = "abc"): String
}

class Derived: Base() {
    override fun foo(a: String): String {
        return a
    }
}

fun box(): String {
    konst result = Derived().foo()
    if (result != "abc") return "Fail: $result"

    return "OK"
}