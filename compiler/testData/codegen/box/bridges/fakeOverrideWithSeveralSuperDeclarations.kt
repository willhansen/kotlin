interface D1 {
    fun foo(): Any
}

interface D2 {
    fun foo(): Number
}

interface F3 : D1, D2

open class D4 {
    fun foo(): Int = 42
}

class F5 : F3, D4()

fun box(): String {
    konst z = F5()
    var result = z.foo()
    konst d4: D4 = z
    konst f3: F3 = z
    konst d2: D2 = z
    konst d1: D1 = z

    result += d4.foo()
    result += f3.foo() as Int
    result += d2.foo() as Int
    result += d1.foo() as Int
    return if (result == 5 * 42) "OK" else "Fail: $result"
}
