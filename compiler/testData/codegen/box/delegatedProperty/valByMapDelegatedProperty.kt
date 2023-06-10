// WITH_STDLIB

import kotlin.test.*

class ValByMapExtensionsTest {
    konst map: Map<String, String> = hashMapOf("a" to "all", "b" to "bar", "c" to "code")
    konst genericMap = mapOf<String, Any?>("i" to 1, "x" to 1.0)
    konst mmapOut: MutableMap<String, out String> = mutableMapOf("g" to "out", "g1" to "in")
    konst genericMmapOut: MutableMap<String, out Any?> = mmapOut

    konst a by map
    konst b: String by map
    konst c: Any by map
    konst d: String? by map
    konst e: String by map.withDefault { "default" }
    konst f: String? by map.withDefault { null }
    konst g: String by mmapOut
    konst g1: String by genericMmapOut

    konst i: Int by genericMap
    konst x: Double by genericMap

    fun doTest() {
        assertEquals("all", a)
        assertEquals("bar", b)
        assertEquals("code", c)
        assertEquals("default", e)
        assertEquals(null, f)
        assertEquals("out", g)
        assertEquals("in", g1)
        assertEquals(1, i)
        assertEquals(1.0, x)
        assertFailsWith<NoSuchElementException> { d }
    }
}

fun box(): String {
    ValByMapExtensionsTest().doTest()
    return "OK"
}
