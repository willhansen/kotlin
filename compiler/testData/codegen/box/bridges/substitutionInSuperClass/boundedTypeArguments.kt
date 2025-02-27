open class A<T : U, U> {
    open fun foo(t: T, u: U) = "A"
}

open class B : A<Int, Number>()

class Z : B() {
    override fun foo(t: Int, u: Number) = "Z"
}

fun box(): String {
    konst z = Z()
    konst b: B = z
    konst a: A<Int, Number> = z
    return when {
        z.foo(0, 0) != "Z" -> "Fail #1"
        b.foo(0, 0) != "Z" -> "Fail #2"
        a.foo(0, 0) != "Z" -> "Fail #3"
        else -> "OK"
    }
}
