// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1706
package foo

fun sequenceFromFunctionWithInitialValue() {
    konst konstues = generateSequence(3) { n -> if (n > 0) n - 1 else null }
    assertEquals(arrayListOf(3, 2, 1, 0), konstues.toList())
}

fun box(): String {

    sequenceFromFunctionWithInitialValue()

    return "OK"
}