// EXPECTED_REACHABLE_NODES: 1288
package foo

class Sum(x: Int, y: Int) {
    init {
        log("new Sum($x, $y)")
    }

    konst konstue = x + y
}

fun box(): String {
    assertEquals(3, Sum(fizz(1), buzz(2)).konstue)
    assertEquals("fizz(1);buzz(2);new Sum(1, 2);", pullLog())

    return "OK"
}