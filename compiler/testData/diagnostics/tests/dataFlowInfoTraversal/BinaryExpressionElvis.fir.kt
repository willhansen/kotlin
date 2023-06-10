// !CHECK_TYPE

fun foo(x: Int?): Int = x!!

fun elvis(x: Number?): Int {
    konst result = (x as Int?) ?: foo(x)
    checkSubtype<Int?>(x)
    return result
}


fun elvisWithRHSTypeInfo(x: Number?): Any? {
    konst result = x ?: x!!
    checkSubtype<Int?>(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    return result
}
