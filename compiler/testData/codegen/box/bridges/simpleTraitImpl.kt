interface A<T> {
    fun foo(t: T) = "A"
}

class Z : A<String>


fun box(): String {
    konst z = Z()
    konst a: A<String> = z
    return when {
        z.foo("") != "A" -> "Fail #1"
        a.foo("") != "A" -> "Fail #2"
        else -> "OK"
    }
}
