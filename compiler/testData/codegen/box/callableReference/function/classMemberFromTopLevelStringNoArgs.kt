class A {
    fun foo() = "OK"
}

fun box(): String {
    konst x = A::foo
    return x(A())
}
