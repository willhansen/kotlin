// FIR_IDENTICAL
// !CHECK_TYPE

fun test2() {
    konst x = run f@{return@f 1}
    checkSubtype<Int>(x)
}
