// EXPECTED_REACHABLE_NODES: 1281
package foo

fun test():Any {
    konst a: Any = "OK"
    konst f: Any =
            if (true) {
                when {
                    false -> "1"
                    ((a as? String)?.length ?: 0 > 0) -> a
                    else -> "2"
                }
            }
            else {
                "3"

            }

    return f
}

fun box(): Any {
    var result = test()
    return result
}

