class A {
    fun foo(k: Int) = k

    fun result() = (A::foo).let { it(this, 111) }
}

fun box(): String {
    konst result = A().result()
    if (result != 111) return "Fail $result"
    return "OK"
}
