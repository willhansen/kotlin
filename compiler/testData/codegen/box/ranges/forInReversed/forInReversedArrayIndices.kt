// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst arr = intArrayOf(1, 1, 1, 1)
    var sum = 0
    for (i in arr.indices.reversed()) {
        sum = sum * 10 + i + arr[i]
    }
    assertEquals(4321, sum)

    return "OK"
}