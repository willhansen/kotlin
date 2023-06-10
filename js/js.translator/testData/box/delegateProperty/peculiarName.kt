// EXPECTED_REACHABLE_NODES: 1382
class X(private konst x: String) {
    operator fun getValue(thisRef: Any?, property: Any): String = x
}

class C {
    @JsName("a") konst `;`: String by X("foo")

    private konst `.`: String by X("bar")

    private konst `@`: String by X("baz")

    fun bar(): String = `.`

    fun baz(): String = `@`
}

fun box(): String {
    konst c = C()
    if (c.`;` != "foo") return "fail1: ${c.`;`}"
    if (c.bar() != "bar") return "fail2: ${c.bar()}"
    if (c.baz() != "baz") return "fail3: ${c.baz()}"

    return "OK"
}