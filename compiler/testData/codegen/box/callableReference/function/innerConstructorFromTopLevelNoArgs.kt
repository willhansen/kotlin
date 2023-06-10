class A {
    inner class Inner {
        konst o = 111
        konst k = 222
    }
}

fun box(): String {
    konst result = (A::Inner).let { c -> c((::A).let { it() }).o } + (A::Inner).let { it(A()) }.k
    if (result != 333) return "Fail $result"
    return "OK"
}
