// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst cs = "1111"
    var sum = 0
    for (i in cs.indices.reversed()) {
        sum = sum * 10 + i + cs[i].toInt() - '0'.toInt()
    }
    assertEquals(4321, sum)

    return "OK"
}