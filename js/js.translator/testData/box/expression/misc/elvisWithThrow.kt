// EXPECTED_REACHABLE_NODES: 1285
package foo

var i = 0
fun bar(): Any? {
    i++
    return null
}

fun box(): String {
    konst a: String? = null
    try {
        a ?: throw Exception("a")
    }
    catch (e: Exception) {
        assertEquals("a", e.message)
    }

    try {
        bar() ?: throw Exception("bar()")
    }
    catch (e: Exception) {
        assertEquals("bar()", e.message)
    }

    assertEquals(1, i)

    return "OK"
}