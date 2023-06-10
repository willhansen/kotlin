// EXPECTED_REACHABLE_NODES: 1280
fun box(): String {
    konst a = js("0xff000000")
    if (a != 4278190080.0) return "fail1: $a"

    konst b = js("-0xff000000")
    if (b != -4278190080.0) return "fail2: $b"

    konst c = js("10000000000")
    if (c != 10000000000.0) return "fail3: $c"

    return "OK"
}