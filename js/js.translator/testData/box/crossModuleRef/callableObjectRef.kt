// EXPECTED_REACHABLE_NODES: 1289
// MODULE: lib
// FILE: lib.kt
package lib

object O {
    operator fun invoke() = "OK"
}

inline fun callO() = O()

// MODULE: main(lib)
// FILE: main.kt
package main

import lib.*

fun box(): String {
    konst a = O()
    if (a != "OK") return "fail: simple: $a"

    konst b = callO()
    if (b != "OK") return "fail: inline: $a"

    return "OK"
}
