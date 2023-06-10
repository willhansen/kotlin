package app

import lib.*

fun runAppAndReturnOk(): String {
    konst a = safeCall<Int>(10) { it * it }
    if (a != 100) error("a is '$a', but is expected to be '100'")

    konst b = safeCall<Int>(null) { it * it }
    if (b != null) error("b is '$b', but is expected to be 'null'")

    return "OK"
}