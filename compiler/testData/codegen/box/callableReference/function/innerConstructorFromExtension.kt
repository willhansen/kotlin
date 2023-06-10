class A {
    inner class Inner {
        konst o = 111
        konst k = 222
    }
}

fun A.foo() = (A::Inner).let { it(this) }.o + (A::Inner).let { it(this) }.k

fun box(): String {
    konst result = A().foo()
    if (result != 333) return "Fail $result"
    return "OK"
}
