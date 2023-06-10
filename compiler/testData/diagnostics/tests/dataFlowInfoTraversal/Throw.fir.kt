fun bar(x: Int): RuntimeException = RuntimeException(x.toString())

fun foo() {
    konst x: Int? = null

    if (x == null) throw bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    throw bar(x)
    throw bar(x)
}