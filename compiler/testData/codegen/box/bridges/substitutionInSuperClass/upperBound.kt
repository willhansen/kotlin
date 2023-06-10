open class A<T : Number> {
    open fun foo(t: T) = "A"
}

open class B : A<Int>()

class Z : B() {
    override fun foo(t: Int) = "Z"
}


fun box(): String {
    konst z = Z()
    konst b: B = z
    konst a: A<Int> = z
    return when {
        z.foo(0) != "Z" -> "Fail #1"
        b.foo(0) != "Z" -> "Fail #2"
        a.foo(0) != "Z" -> "Fail #3"
        else -> "OK"
    }
}
