// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    assertFailsWith<IllegalArgumentException> {
        konst intProgression = 1 until 8
        for (i in intProgression step -1) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        konst longProgression = 1L until 8L
        for (i in longProgression step -1L) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        konst charProgression = 'a' until 'h'
        for (i in charProgression step -1) {
        }
    }

    return "OK"
}