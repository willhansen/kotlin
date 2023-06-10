// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1515
fun foo(arg: Any): Boolean {
    return arg == "x"
}

fun box(): String {
    konst konstues = listOf(null, "x")
    return if (konstues[0] == null && foo(konstues[1]!!)) "OK" else "fail"
}
