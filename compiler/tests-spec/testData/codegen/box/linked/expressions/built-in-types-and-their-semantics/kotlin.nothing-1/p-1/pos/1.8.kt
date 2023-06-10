// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: expressions, built-in-types-and-their-semantics, kotlin.nothing-1 -> paragraph 1 -> sentence 1
 * NUMBER: 8
 * DESCRIPTION: check kotlin.Nothing type
 */

fun box(): String {
    konst c = Class()
    try {
        konst a: () -> Nothing = c.data
        a.invoke()
    } catch (e: IllegalArgumentException) {
        try {
            konst d: () -> Nothing = c.konstue
            d()
        } catch (e: NotImplementedError) {
            return "OK"
        }

    }
    return "NOK"
}

open class Class() {
    var data: () -> Nothing = { throwException("data") as Nothing }
    konst konstue: () -> Nothing
        get() = { TODO() as Nothing }
}

private fun throwException(m: String): Any {
    throw  IllegalArgumentException(m)
}
