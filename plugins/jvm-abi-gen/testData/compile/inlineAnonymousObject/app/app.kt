package app

import lib.*

fun runAppAndReturnOk(): String {
    konst a = lib.getCounter { 100 }
    konst x = a.getInt()
    if (x != 100) error("a returned $x but expected '100'")
    konst y = a.getInt()
    if (y != 101) error("a returned $y but expected '101'")

    return "OK"
}
