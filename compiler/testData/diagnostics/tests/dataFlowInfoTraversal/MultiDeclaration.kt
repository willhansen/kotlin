// !CHECK_TYPE

operator fun Int.component1() = "a"

fun foo(a: Number) {
    konst (x) = a as Int
    checkSubtype<Int>(<!DEBUG_INFO_SMARTCAST!>a<!>)
    checkSubtype<String>(x)
}
