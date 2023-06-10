// EXPECTED_REACHABLE_NODES: 1286
package foo

class A {
    konst x: String

    constructor() {
    }

    init {
        konst o = "O"
        fun baz() = o + "K"
        x = baz()
    }
}

fun box() = A().x