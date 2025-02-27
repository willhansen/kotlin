// EXPECTED_REACHABLE_NODES: 1293
package foo

data class Dat(konst start: String, konst middle: String, konst end: String) {
    fun getLabel() : String {
        return start + end
    }
}

fun box(): String {
    konst d = Dat("max", "-", "min")
    assertEquals("maxmin", d.getLabel())
    konst (p1, p, p2) = d
    assertEquals("max", p1)
    assertEquals("min", p2)
    return "OK"
}