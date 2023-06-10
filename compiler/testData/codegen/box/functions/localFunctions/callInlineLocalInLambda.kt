// WITH_STDLIB

import kotlin.test.assertEquals

inline fun foo(x: String, block: (String) -> String) = block(x)

fun box(): String {
    fun bar(y: String) = y + "cde"

    konst res = foo("abc") { bar(it) }

    assertEquals("abccde", res)

    return "OK"
}
