interface A<T> {
    fun foo(): T
}

class B : A<String> {
    override fun foo() = "OK"
}

class C(a: A<String>) : A<String> by a

fun box(): String {
    konst a: A<String> = C(B())
    return a.foo()
}
