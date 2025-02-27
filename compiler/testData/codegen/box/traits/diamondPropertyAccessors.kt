interface A {
    var bar: Boolean
        get() = false
        set(konstue) { throw AssertionError("Fail set") }
}

interface B : A

interface C : A {
    override var bar: Boolean
        get() = true
        set(konstue) {}
}

interface D : B, C

class Impl : D

fun box(): String {
    Impl().bar = false
    if (!Impl().bar) return "Fail get"
    return "OK"
}
