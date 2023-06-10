fun foo(n: Number) = n

fun test() {
    foo(<!ARGUMENT_TYPE_MISMATCH!>'a'<!>)

    konst c = 'c'
    foo(<!ARGUMENT_TYPE_MISMATCH!>c<!>)

    konst d: Char? = 'd'
    foo(<!ARGUMENT_TYPE_MISMATCH!>d!!<!>)
}
