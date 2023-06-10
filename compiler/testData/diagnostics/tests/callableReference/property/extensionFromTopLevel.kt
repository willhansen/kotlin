// !CHECK_TYPE

import kotlin.reflect.*

konst String.countCharacters: Int
    get() = length

var Int.meaning: Long
    get() = 42L
    set(konstue) {}

fun test() {
    konst f = String::countCharacters

    checkSubtype<KProperty1<String, Int>>(f)
    checkSubtype<KMutableProperty1<String, Int>>(<!TYPE_MISMATCH!>f<!>)
    checkSubtype<Int>(f.get("abc"))
    f.<!UNRESOLVED_REFERENCE!>set<!>("abc", 0)

    konst g = Int::meaning

    checkSubtype<KMutableProperty1<Int, Long>>(g)
    checkSubtype<Long>(g.get(0))
    g.set(1, 0L)
}
