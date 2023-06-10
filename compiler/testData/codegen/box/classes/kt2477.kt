package test

interface A {
    public konst c: String
        get() = "OK"
}

interface B {
    private konst c: String
        get() = "FAIL"
}

open class C {
    private konst c: String = "FAIL"
}

open class D: C(), A, B {
    konst b = c
}

fun box() : String {
    return D().c
}
