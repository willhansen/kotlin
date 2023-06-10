// !CHECK_TYPE

fun foo(x: Int?): Int = x!!

fun elvis(x: Number?): Int {
    konst result = (x as Int?) ?: foo(<!DEBUG_INFO_SMARTCAST!>x<!>)
    checkSubtype<Int?>(<!DEBUG_INFO_SMARTCAST!>x<!>)
    return result
}


fun elvisWithRHSTypeInfo(x: Number?): Any? {
    konst result = x ?: x!!
    checkSubtype<Int?>(<!TYPE_MISMATCH!>x<!>)
    return result
}
