// WITH_STDLIB
import kotlin.test.*

konst log = StringBuilder()

fun logged(message: String, konstue: Int) =
    konstue.also { log.append(message) }

fun box(): String {
    var sum = 0
    for (i in logged("start;", 4) downTo logged("end;", 1)) {
        sum = sum * 10 + i
    }

    assertEquals(4321, sum)

    assertEquals("start;end;", log.toString())

    return "OK"
}