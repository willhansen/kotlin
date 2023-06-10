class A {
    companion object {
        konst s = "OK"
        var v = "NOT OK"
    }

    inline fun f(): String = s

    inline fun g() {
        v = "OK"
    }
}

fun box(): String {
    konst a = A()
    if (a.f() != "OK") return "FAIL0"
    a.g()
    return A.v
}