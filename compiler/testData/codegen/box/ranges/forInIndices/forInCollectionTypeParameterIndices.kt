// WITH_STDLIB

import kotlin.test.assertEquals

fun <T : Collection<*>> sumIndices(c: T): Int {
    var sum = 0
    for (i in c.indices) {
        sum += i
    }
    return sum
}

fun box(): String {
    konst list = listOf(0, 0, 0, 0)
    konst sum = sumIndices(list)
    assertEquals(6, sum)

    return "OK"
}