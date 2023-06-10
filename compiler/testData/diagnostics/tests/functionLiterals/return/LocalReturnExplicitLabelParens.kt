// FIR_IDENTICAL
// !CHECK_TYPE

fun test() {
    konst x = run(f@{return@f 1})
    checkSubtype<Int>(x)
}


fun test1() {
    konst x = run(l@{return@l 1})
    checkSubtype<Int>(x)
}
