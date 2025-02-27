open class A<T> {
    open fun foo(t: T) = "A"
}

open class B<T> : A<T>()

open class C : B<String>() {
    override fun foo(t: String) = "C"
}

open class D : C()

class Z : D() {
    override fun foo(t: String) = "Z"
}


fun box(): String {
    konst z = Z()
    konst d: D = z
    konst c: C = z
    konst b: B<String> = z
    konst a: A<String> = z
    return when {
        z.foo("") != "Z" -> "Fail #1"
        d.foo("") != "Z" -> "Fail #2"
        c.foo("") != "Z" -> "Fail #3"
        b.foo("") != "Z" -> "Fail #4"
        a.foo("") != "Z" -> "Fail #5"
        else -> "OK"
    }
}
