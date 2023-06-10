// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1368

package foo

fun box(): String {
    konst map = mapOf(1 to "")
    konst i = 1
    return when (i) {
        in map -> "OK"
        else -> "fail"
    }
}