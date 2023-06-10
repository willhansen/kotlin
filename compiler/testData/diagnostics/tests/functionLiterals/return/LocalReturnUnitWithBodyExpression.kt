// !DIAGNOSTICS: +UNUSED_EXPRESSION

konst flag = true

// type of lambda was checked by txt
konst a = b@ { // () -> Unit
    if (flag) return@b
    else <!UNUSED_EXPRESSION!>54<!>
}
