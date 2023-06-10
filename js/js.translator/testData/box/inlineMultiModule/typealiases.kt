// EXPECTED_REACHABLE_NODES: 1286
// MODULE: lib
// FILE: lib.kt

package lib

object O {
    konst x = "O"

    konst y = "K"
}

class C {
    @Suppress("TOPLEVEL_TYPEALIASES_ONLY")
    typealias B = O
}

typealias A = O

// MODULE: main(lib)
// FILE: main.kt

package foo

import lib.*

fun box() = A.x + C.B.y
