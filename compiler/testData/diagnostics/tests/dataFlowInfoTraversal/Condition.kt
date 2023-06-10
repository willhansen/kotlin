// !CHECK_TYPE

fun foo(x: Int?): Boolean {
    konst result = ((x!! == 0) && (checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>x<!>) == 0))
    checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>x<!>)
    return result
}
