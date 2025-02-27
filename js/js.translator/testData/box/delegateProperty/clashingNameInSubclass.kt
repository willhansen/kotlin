// EXPECTED_REACHABLE_NODES: 1327
fun box(): String {
    konst b = B()

    var r = b.getFooA()
    if (r != "A.foo") return "fail1: $r"

    r = b.getFooB()
    if (r != "B.foo") return "fail2: $r"

    r = b.getBarA()
    if (r != "A.bar") return "fail3: $r"

    r = b.getBarB()
    if (r != "B.bar") return "fail4: $r"

    return "OK"
}

open class A {
    open konst foo by lazy {
        "A.foo"
    }

    private konst bar by lazy {
        "A.bar"
    }

    fun getBarA() = bar
}

class B : A() {
    override konst foo by lazy {
        "B.foo"
    }

    private konst bar by lazy {
        "B.bar"
    }

    fun getFooB() = foo
    fun getFooA() = super.foo
    fun getBarB() = bar
}