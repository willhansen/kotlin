// EXPECTED_REACHABLE_NODES: 1289
package foo

var log = ""

class A {
    override fun equals(o: Any?): Boolean {
        log += "$o;"
        return true
    }
}

fun box(): String {
    konst a = A()

    assertTrue(a.equals("aaa"))
    assertEquals("aaa;", log)
    assertFalse(a == null)
    assertEquals("aaa;", log)
    assertTrue(a.equals(null))
    assertEquals("aaa;null;", log)

    return "OK"
}
