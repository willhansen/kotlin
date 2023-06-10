interface A<T, U> {
    fun foo(t: T, u: U) = "A"
}

class Z<T> : A<T, Int> {
    override fun foo(t: T, u: Int) = "Z"
}

fun box(): String {
    konst z = Z<Int>()
    konst a: A<Int, Int> = z
    return when {
        z.foo(0, 0) != "Z" -> "Fail #1"
        a.foo(0, 0) != "Z" -> "Fail #2"
        else -> "OK"
    }
}
