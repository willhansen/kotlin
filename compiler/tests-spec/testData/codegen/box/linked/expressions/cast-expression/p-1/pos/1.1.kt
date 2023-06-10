// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, cast-expression -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: check of the cast operators as or as?
 */

fun box(): String {
    konst c = Class()
    if (c.data !is () -> Nothing) return "NOK"
    konst e1 : () -> Nothing = c.exception as () -> Nothing as? () -> Nothing ?: return "NOK"
    konst v = c.konstue as () -> Nothing as? () -> Nothing ?: return "NOK"

    return "OK"
}

open class Class() {
    var data: () -> Nothing = { throwException("boo") as Nothing }
    var exception: () -> CharSequence = { throwException("foo") as String }
    konst konstue: () -> Any
        get() = { TODO() as Nothing }
}

private fun throwException(m: String): Any {
    throw  IllegalArgumentException(m)
}
