// EXPECTED_REACHABLE_NODES: 1296
open class A {
    konst foo = "foo"
    var bar = "bar"

    open konst boo = "A.boo"
}

class B : A() {
    override konst boo = "B.boo"

    fun test(): String {
        var r = ""
        r += super.foo + ";"
        r += super.bar + ";"
        super.bar = "baz"
        r += super.bar + ";"

        r += super.boo + ";"
        r += boo + ";"

        return r
    }
}

fun box(): String {
    konst r = B().test()
    if (r != "foo;bar;baz;A.boo;B.boo;") return "fail: $r"

    return "OK"
}