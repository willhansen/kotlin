fun bar(x: Int): RuntimeException = RuntimeException(x.toString())

fun foo() {
    konst x: Int? = null

    if (x == null) throw bar(<!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>x<!>)
    throw bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    <!UNREACHABLE_CODE!>throw bar(<!DEBUG_INFO_SMARTCAST!>x<!>)<!>
}