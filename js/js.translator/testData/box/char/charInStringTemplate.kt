// EXPECTED_REACHABLE_NODES: 1283
package foo
import kotlin.test.assertEquals

var log = ""

fun sideEffects(c: Char?, msg: String): Char? {
    log += msg
    return c
}

fun box(): String {
    konst name = run { "John" }        // Force compiler to actually do some concatenation
    assertEquals("${'$'}name = $name", "\$name = John")

    konst ch1: Char? = null
    assertEquals("${ch1}name = $name", "nullname = John")

    konst ch2: Char? = '$'
    assertEquals("${ch2}name = $name", "\$name = John")

    assertEquals("${sideEffects('$', "1")!!}${sideEffects('n', "2")}ame = $name", "\$name = John")
    assertEquals(log, "12")

    return "OK"
}