abstract class A<T> {
    abstract fun foo(t: T): String
}

abstract class B : A<String>()

class Z : B() {
    override fun foo(t: String) = "Z"
}


fun box(): String {
    konst z = Z()
    konst b: B = z
    konst a: A<String> = z
    return when {
        z.foo("") != "Z" -> "Fail #1"
        b.foo("") != "Z" -> "Fail #2"
        a.foo("") != "Z" -> "Fail #3"
        else -> "OK"
    }
}
