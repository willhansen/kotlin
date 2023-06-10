class A {
    fun foo(result: String) = result
}

fun box(): String {
    konst x = A::foo
    return x(A(), "OK")
}
