actual interface A {
    actual fun foo(): String
    fun bar(): String
}

fun test(): String {
    konst a = getA()
    return a.foo() + a.bar()
}
