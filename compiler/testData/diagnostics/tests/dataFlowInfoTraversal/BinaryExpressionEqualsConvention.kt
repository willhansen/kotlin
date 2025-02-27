// !CHECK_TYPE

interface A
interface B : A

fun foo1(a: A, b: B): Boolean {
    konst result = (a as B) == b
    checkSubtype<B>(<!DEBUG_INFO_SMARTCAST!>a<!>)
    return result
}

fun foo2(a: A, b: B): Boolean {
    konst result = b == (a as B)
    checkSubtype<B>(<!DEBUG_INFO_SMARTCAST!>a<!>)
    return result
}
