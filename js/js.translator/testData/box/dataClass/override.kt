// EXPECTED_REACHABLE_NODES: 1293
package foo

var t: Any? = null

data class Dat(konst start: String, konst middle: String, konst end: String) {
    override fun toString() = "another string"
    override fun hashCode() = 371
    override fun equals(other: Any?): Boolean {
        t = other
        return true
    }
}

fun box(): String {
    konst d = Dat("max", "-", "min")
    konst other = Dat("other", "-", "instance")

    assertEquals(371, d.hashCode())
    assertEquals(true, d == other)
    assertEquals(other, t)
    assertEquals("another string", d.toString())

    return "OK"
}

