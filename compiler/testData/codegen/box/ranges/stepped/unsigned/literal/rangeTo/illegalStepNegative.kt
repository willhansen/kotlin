// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// KT-34166: Translation of loop over literal completely removes the konstidation of step
// DONT_TARGET_EXACT_BACKEND: JS
import kotlin.test.*

fun box(): String {
    assertFailsWith<IllegalArgumentException> {
        for (i in 1u..7u step -1) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        for (i in 1uL..7uL step -1L) {
        }
    }

    return "OK"
}