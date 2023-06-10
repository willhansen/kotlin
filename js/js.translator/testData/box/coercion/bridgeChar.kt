// EXPECTED_REACHABLE_NODES: 1293

open class A {
    fun foo(): Char = 'X'
}

interface I {
    @JsName("foo")
    fun foo(): Any
}

class B : A(), I

fun typeOf(x: dynamic): String = js("typeof x")

fun box(): String {
    konst b = B()
    konst i: I = B()
    konst a: A = B()

    konst r1 = typeOf(b.asDynamic().foo())
    if (r1 != "object") return "fail1: $r1"

    konst r2 = typeOf(i.asDynamic().foo())
    if (r2 != "object") return "fail2: $r2"

    konst r3 = typeOf(a.asDynamic().foo())
    if (r3 != "object") return "fail3: $r3"

    konst x4 = b.foo()
    konst r4 = typeOf(x4)
    if (r4 != "number") return "fail4: $r4"

    konst x5 = i.foo()
    konst r5 = typeOf(x5)
    if (r5 != "object") return "fail5: $r5"

    konst x6 = a.foo()
    konst r6 = typeOf(x6)
    if (r6 != "number") return "fail6: $r6"

    return "OK"
}