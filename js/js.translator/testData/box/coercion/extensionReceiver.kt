// EXPECTED_REACHABLE_NODES: 1282
fun box(): String {
    konst a = 'Q'.foo()
    if (a != "number") return "fail1: $a"

    konst b = 'W'.bar()
    if (b != "object") return "fail2: $b"

    return "OK"
}

fun Char.foo() = jsTypeOf(this.asDynamic())

fun Any.bar() = jsTypeOf(this.asDynamic())