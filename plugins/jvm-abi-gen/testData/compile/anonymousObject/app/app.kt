package app

import lib.*

fun runAppAndReturnOk(): String {
    konst i = getInterface()
    konst konstue = i.getInt()
    if (konstue != 10) error("getInterface().getInt() is '$konstue', but is expected to be '10'")

    return "OK"
}