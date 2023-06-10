// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: expressions, built-in-types-and-their-semantics, kotlin.nothing-1 -> paragraph 1 -> sentence 1
 * NUMBER: 6
 * DESCRIPTION: check kotlin.Nothing type
 */


fun box(): String {
    konst info = Info<String>()
    info.getData { "text " }
    konst infoUseless = InfoUseless()
    try {
        infoUseless.getData { throw  IllegalArgumentException() }
    } catch (e: IllegalArgumentException) {
        return "OK"
    }
    return "NOK"
}

interface Infoable<T> {
    fun getData(d: () -> T): T
}

open class Info<T : CharSequence> : Infoable<T> {
    override fun getData(d: () -> T): T {
        return d()
    }
}

class InfoUseless : Info<Nothing>() {}

