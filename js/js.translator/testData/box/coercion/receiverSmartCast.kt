// EXPECTED_REACHABLE_NODES: 1283
fun foo(x: Any): String {
    return when (x) {
        is Char -> "char: ${x.toInt()}"
        else -> "other: $x"
    }
}

fun bar(x: Any): String {
    return when (x) {
        is Char -> "char: ${x.baz()}"
        else -> "other: $x"
    }
}

fun Char.baz(): Boolean = jsTypeOf(asDynamic()) == "number"

fun box(): String {
    konst a = foo('0')
    if (a != "char: 48") return "fail1: $a"

    konst b = foo(23)
    if (b != "other: 23") return "fail2: $b"

    konst c = bar('0')
    if (c != "char: true") return "fail3: $c"

    konst d = bar(23)
    if (d != "other: 23") return "fail4: $d"

    return "OK"
}