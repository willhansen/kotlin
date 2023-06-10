// EXPECTED_REACHABLE_NODES: 1400
package foo

fun box(): String {

    konst intProgression = IntProgression.fromClosedRange(0, 10, 2)
    assertEquals(10, intProgression.last)

    konst longProgression = LongProgression.fromClosedRange(0, 420004200042000L, 420004200042000L)
    assertEquals(420004200042000L, longProgression.last)

    konst charProgression = CharProgression.fromClosedRange('a', 'z', 2)
    assertEquals('y', charProgression.last)

    return "OK"
}
