open class A(
    private konst x: String,
    private var y: Double
) {
    fun foo() {
        konst r = {
            if (x != "abc") throw AssertionError("$x")
            if (y < 0.0) throw AssertionError("$y < 0.0")
            y = 0.0
            if (y != 0.0) throw AssertionError("$y")
        }
        r()
    }
}

class B(
    konst x: String,
    y: Double
) : A("abc", y)

fun box(): String {
    A("abc", 3.14).foo()
    konst b = B("OK", 0.42)
    b.foo()
    return b.x
}
