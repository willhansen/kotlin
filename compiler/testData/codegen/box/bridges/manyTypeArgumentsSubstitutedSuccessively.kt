open class A<T, U, V> {
    open fun foo(t: T, u: U, v: V) = "A"
}

open class B<T, V> : A<T, Int, V>()

open class C<V> : B<String, V>()

class Z : C<Double>() {
    override fun foo(t: String, u: Int, v: Double) = "Z"
}


fun box(): String {
    konst z = Z()
    konst c: C<Double> = z
    konst b: B<String, Double> = z
    konst a: A<String, Int, Double> = z
    return when {
        z.foo("", 0, 0.0) != "Z" -> "Fail #1"
        c.foo("", 0, 0.0) != "Z" -> "Fail #2"
        b.foo("", 0, 0.0) != "Z" -> "Fail #3"
        a.foo("", 0, 0.0) != "Z" -> "Fail #4"
        else -> "OK"
    }
}
