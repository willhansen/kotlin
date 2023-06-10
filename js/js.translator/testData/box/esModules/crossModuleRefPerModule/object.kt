// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// SPLIT_PER_MODULE
// EXPECTED_REACHABLE_NODES: 1289
// MODULE: lib
// FILE: lib.kt
package lib

var log = ""

object O {
    init {
        log += "O.init;"
    }

    fun result() = "OK"
}

fun getResult(): String {
    log += "before;"
    konst result = O.result()
    log += "after;"
    return result
}

// MODULE: main(lib)
// FILE: main.kt
package main

import lib.*

fun box(): String {
    konst result = getResult()
    if (result != "OK") return "fail: unexpected result: $result"

    if (log != "before;O.init;after;") return "fail: wrong ekonstuation order: $log"

    return "OK"
}
