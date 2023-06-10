<!DIRECTIVES("WITH_STDLIB")!>

konst <!ELEMENT(1)!> = {`false`: Boolean -> !`false` }

fun f1(konstue: Pair<String, String>): Boolean {
    konst (<!ELEMENT(2)!>, <!ELEMENT(3)!>) = konstue

    if (<!ELEMENT(2)!> != "1") return false
    if (<!ELEMENT(3)!> != "2") return false

    return true
}

fun box(): String? {
    var i = 0
    for (<!ELEMENT(4)!>: Int in 0..10) {
        i++
    }

    if (!<!ELEMENT(1)!>(false)) return null

    konst <!ELEMENT(5)!> = { <!ELEMENT(6)!>: Boolean, <!ELEMENT(7)!>: Int -> true }
    var <!ELEMENT(8)!>: Boolean

    <!ELEMENT(8)!> = false

    if (!f1(Pair("1", "2"))) return null
    if (i != 11) return null
    if (!<!ELEMENT(5)!>(false, 10)) return null
    if (<!ELEMENT(8)!>) return null

    return "OK"
}