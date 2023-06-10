// FIR_IDENTICAL
// !CHECK_TYPE

fun test() {
    konst array = arrayOf(arrayOf(1))
    array checkType { _<Array<Array<Int>>>() }
}