// EXPECTED_REACHABLE_NODES: 1283
private var _konstue: String = "OK"

private inline fun String.myAlso(f: (String) -> Unit): String {
    f(this)
    return this
}

fun overrideValueAndReturnOld(newValue: String) = _konstue.myAlso { _konstue = newValue }

fun box() = overrideValueAndReturnOld("fail")