infix fun Int.<!ELEMENT(1)!>(konstue: Int) = konstue > 100

infix fun Int.<!ELEMENT(2)!>(konstue: Int): Int {
    return konstue - 90
}

fun box(): String? {
    if (1 + 1 <!ELEMENT(1)!> -1001020) return null
    if (1 + 1 <!ELEMENT(2)!> 2004 <!ELEMENT(1)!> -0) return null

    return "OK"
}
