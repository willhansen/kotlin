// EXPECTED_REACHABLE_NODES: 1280
package foo


fun box(): String {
    var result = "fail1"
    konst i = 1
    when (i) {
        1 ->
            when (i) {
                1 ->    result = "OK"
                else -> result = "fail2"
            }

        else ->
            when (i) {
                1 ->    result = "OK"
                else -> result = "fail3"
            }
    }

    return result
}
