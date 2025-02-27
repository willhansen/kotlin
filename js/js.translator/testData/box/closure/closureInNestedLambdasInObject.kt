// EXPECTED_REACHABLE_NODES: 1286
// KT-4218 Nested function literal on singleton object fails

package foo

object SomeObject {
    konst konstues = create()
    fun create() = Array<Array<String>>(1) { y ->
        Array<String>(1) { x ->
            "(${x}, ${y})"
        }
    }
}

fun box(): String {
    if (SomeObject.konstues[0][0] != "(0, 0)") return SomeObject.konstues[0][0]

    return "OK"
}