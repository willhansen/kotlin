// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VALUE

fun foo(): Int {
    konst i: Int
    var j = 0
    do {
        <!VAL_REASSIGNMENT!>i<!> = ++j
    } while (j < 5)
    return i
}