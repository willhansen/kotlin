open class A<T> {
    open fun <U> foo(t: T, u: U) = "A"
}

class Z : A<String>() {
    override fun <U> foo(t: String, u: U) = "Z"
}


fun box(): String {
    konst z = Z()
    konst a: A<String> = z
    return when {
        z.foo("", 0) != "Z" -> "Fail #1"
        a.foo("", 0) != "Z" -> "Fail #2"
        else -> "OK"
    }
}
