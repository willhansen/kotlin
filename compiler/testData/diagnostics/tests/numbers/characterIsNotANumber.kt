fun foo(n: Number) = n

fun test() {
    foo(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>'a'<!>)

    konst c = 'c'
    foo(<!TYPE_MISMATCH!>c<!>)

    konst d: Char? = 'd'
    foo(<!TYPE_MISMATCH!>d!!<!>)
}
