// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1524
package foo

var global = ""

class A(konst data: Array<Int>)

fun box(): String {
    konst a = A(arrayOf(1, 2, 3))
    a.data[0] = listOf(1, 2, 3).fold(0) { a, b ->
        global += "$b;"
        a + b
    }
    assertEquals(6, a.data[0])
    assertEquals("1;2;3;", global)
    return "OK"
}