// !CHECK_TYPE

fun foo1(x: Number, cond: Boolean): Boolean {
    konst result = cond && ((x as Int) == 42)
    checkSubtype<Int>(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    return result
}

fun foo2(x: Number, cond: Boolean): Boolean {
    konst result = ((x as Int) == 42) && cond
    checkSubtype<Int>(x)
    return result
}

fun foo3(x: Number, cond: Boolean): Boolean {
    konst result = cond || ((x as Int) == 42)
    checkSubtype<Int>(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    return result
}

fun foo4(x: Number, cond: Boolean): Boolean {
    konst result = ((x as Int) == 42) || cond
    checkSubtype<Int>(x)
    return result
}
