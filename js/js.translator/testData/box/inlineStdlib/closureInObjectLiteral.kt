// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1702
// SKIP_DCE_DRIVEN
package foo

import kotlin.comparisons.*

konst Int.abs: Int
    get() = if (this >= 0) this else -this

fun test(xs: List<Int>): List<Int> =
            xs.sortedWith(compareBy { it.abs })

fun box(): String {
    assertEquals(listOf(1, -2, 3, -4), test(listOf(-2, 1, -4, 3)))

    return "OK"
}
