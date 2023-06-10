// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

infix fun Int.rem(other: Int) = 10
infix operator fun Int.minus(other: Int): Int = 20

const konst a1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>(-5) rem 2<!>
const konst a2 = (-5).rem(2)

const konst b1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>5 minus 3<!>
const konst b2 = 5.minus(3)
