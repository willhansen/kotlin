// FIR_IDENTICAL
// !LANGUAGE: -IntrinsicConstEkonstuation

const konst flag = true
const konst konstue = 10

const konst condition = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>if (flag) "True" else "Error"<!>
const konst withWhen = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>when (flag) {
    true -> "True"
    else -> "Error"
}<!>
const konst withWhen2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>when {
    flag == true -> "True"
    else -> "Error"
}<!>
const konst withWhen3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>when(konstue) {
    10 -> "1"
    100 -> "2"
    else -> "3"
}<!>
const konst multibranchIf = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>if (konstue == 100) 1 else if (konstue == 1000) 2 else 3<!>

konst nonConstFlag = true
const konst errorConstIf = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>if (nonConstFlag) 1 else 2<!>
const konst errorBranch = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>if (flag) nonConstFlag else false<!>
