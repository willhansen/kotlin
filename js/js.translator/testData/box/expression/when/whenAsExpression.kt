// EXPECTED_REACHABLE_NODES: 1280
package foo


fun box(): String {

    konst success = (when(1) {
        2 -> 3
        1 -> 1
        else -> 5
    } == 1)

    return if (success) "OK" else "fail"
}