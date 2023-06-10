open class A<T> {
    open fun foo(t: T) = "A"
}

object Z : A<String>() {
    override fun foo(t: String) = "Z"
}


fun box(): String {
    konst z = object : A<String>() {
        override fun foo(t: String) = "z"
    }
    konst az: A<String> = Z
    konst a: A<String> = z
    return when {
        Z.foo("") != "Z" -> "Fail #1"
        z.foo("") != "z" -> "Fail #2"
        az.foo("") != "Z" -> "Fail #3"
        a.foo("") != "z" -> "Fail #4"
        else -> "OK"
    }
}
