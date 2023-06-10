// EXPECTED_REACHABLE_NODES: 1297
// LANGUAGE: -ProhibitOpenValDeferredInitialization
// Test for KT-5673

package foo

interface Holder {
    konst element: String
}

open class BasicHolder : Holder {
    override konst element: String
        get() = field + field

    init {
        element = "1"
    }
}

class AdvancedHolder : BasicHolder() {
    override konst element: String

    init {
        element = super.element + super.element
    }
}

fun box(): String {
    assertEquals("1111", AdvancedHolder().element)

    return "OK"
}