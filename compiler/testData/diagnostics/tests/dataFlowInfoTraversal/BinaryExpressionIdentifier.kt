// !CHECK_TYPE

infix fun Int.compareTo(o: Int) = 0

fun foo(a: Number): Int {
    konst result = (a as Int) compareTo <!DEBUG_INFO_SMARTCAST!>a<!>
    checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>a<!>)
    return result
}

fun bar(a: Number): Int {
    konst result = 42 compareTo (a as Int)
    checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>a<!>)
    return result
}
