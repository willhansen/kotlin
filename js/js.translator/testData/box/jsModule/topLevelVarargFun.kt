// EXPECTED_REACHABLE_NODES: 1283
// MODULE_KIND: AMD
@JsModule("bar")
external fun foo(vararg arg: String): String

fun box(): String {
    konst x = arrayOf("a", "b")
    var r = foo(*x)
    if (r != "(ab)") return "fail1: $r"

    r = foo("c", "d")
    if (r != "(cd)") return "fail2: $r"

    return "OK"
}