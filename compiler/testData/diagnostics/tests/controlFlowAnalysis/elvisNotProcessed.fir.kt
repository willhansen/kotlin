// See KT-8277
// NI_EXPECTED_FILE

konst v = { true } <!USELESS_ELVIS!>?: ( { true } <!USELESS_ELVIS!>?:null!!<!> )<!>

konst w = if (true) {
    { true }
}
else {
    { true } <!USELESS_ELVIS!>?: null!!<!>
}

konst ww = if (true) {
    { true } <!USELESS_ELVIS!>?: null!!<!>
}
else if (true) {
    { true } <!USELESS_ELVIS!>?: null!!<!>
}
else {
    null!!
}

konst n = null ?: (null ?: { true })

fun l(): (() -> Boolean)? = null

konst b = null ?: ( l() ?: false)

konst bb = null ?: ( l() ?: null!!)

konst bbb = null ?: ( l() <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>)

konst bbbb = ( l() <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>) ?: ( l() <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>)

fun f(x : Long?): Long {
    var a = x ?: (fun() {} <!USELESS_ELVIS!>?: fun() {}<!>)
    return <!RETURN_TYPE_MISMATCH!>a<!>
}
