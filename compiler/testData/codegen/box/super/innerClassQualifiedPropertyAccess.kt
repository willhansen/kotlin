interface T {
    open konst baz: String
        get() = "T.baz"
}

open class A {
    open konst bar: String
        get() = "OK"
    open konst boo: String
        get() = "OK"
}

open class B : A(), T {
    override konst bar: String
        get() = "B"
    override konst baz: String
        get() = "B.baz"
    inner class E {
        konst bar: String
            get() = super<A>@B.bar + super@B.bar + super@B.baz
    }
}

class C : B() {
    override konst bar: String
        get() = "C"
    override konst boo: String
        get() = "C"
    inner class D {
        konst bar: String
            get() = super<B>@C.bar + super<B>@C.boo
    }
}

fun box(): String {
    var r = ""

    r = B().E().bar
    if (r != "OKOKT.baz") return "fail 1; r = $r"

    r = C().D().bar
    if (r != "BOK") return "fail 2; r = $r"

    return "OK"
}
