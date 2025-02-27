// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    konst sb = StringBuilder("1234")
    konst result = StringBuilder()
    var ctr = 0
    for (c in sb) {
        if (ctr % 2 == 0)
            sb.append('x')
        ctr++
        result.append(c)
    }
    assertEquals("1234xxxx", sb.toString())
    assertEquals("1234xxxx", result.toString())

    return "OK"
}