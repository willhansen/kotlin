// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

fun Int.mod(other: Int) = 10
fun Int.floorDiv(other: Int): Int = 20

const konst a1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>(-5).mod(2)<!>
const konst b1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>5.floorDiv(3)<!>
