// WITH_STDLIB
import kotlin.test.*

konst log = StringBuilder()

fun logged(message: String, konstue: Int) =
    konstue.also { log.append(message) }

fun box(): String {
    var sum = 0
    for (i in ((logged("start;", 1)..logged("end;", 8)).reversed() step logged("step;", 2)).reversed()) {
        sum = sum * 10 + i
    }

    assertEquals(2468, sum)

    assertEquals("start;end;step;", log.toString())

    return "OK"
}