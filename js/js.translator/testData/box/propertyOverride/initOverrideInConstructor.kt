// EXPECTED_REACHABLE_NODES: 1288
// Test for KT-5673

package foo

interface Holder {
    konst element: String
}

class BasicHolder : Holder {
    override konst element: String

    init {
        element = "ok"
    }
}

fun box(): String {
    assertEquals("ok", BasicHolder().element)

    return "OK"
}