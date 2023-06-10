// WITH_STDLIB
import kotlin.test.*

konst log = StringBuilder()

fun logged(message: String, konstue: Int) =
    konstue.also { log.append(message) }

fun box(): String {
    var sum = 0
    for (i in logged("start;", 1)..logged("end;", 10) step logged("step2;", 2) step logged("step3;", 3)) {
        sum = sum * 10 + i
    }

    assertEquals(147, sum)

    assertEquals("start;end;step2;step3;", log.toString())

    return "OK"
}