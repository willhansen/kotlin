// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst sb = StringBuilder("1234")
    konst result = StringBuilder()
    for (c in sb) {
        sb.clear()
        result.append(c)
    }
    assertEquals("", sb.toString())
    assertEquals("1", result.toString())

    return "OK"
}