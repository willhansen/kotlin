// !CHECK_TYPE

package m

import checkSubtype

fun test(i: Int?) {
    if (i != null) {
        foo(l1@ i)
        foo((i))
        foo(l2@ (i))
        foo((l3@ i))
    }

    konst a: Int = <!INITIALIZER_TYPE_MISMATCH!>l4@ ""<!>
    konst b: Int = <!INITIALIZER_TYPE_MISMATCH!>("")<!>
    konst c: Int = checkSubtype<Int>(<!ARGUMENT_TYPE_MISMATCH!>""<!>)
    konst d: Int = <!INITIALIZER_TYPE_MISMATCH!>checkSubtype<Long>(<!ARGUMENT_TYPE_MISMATCH!>""<!>)<!>


    foo(l4@ <!ARGUMENT_TYPE_MISMATCH!>""<!>)
    foo((<!ARGUMENT_TYPE_MISMATCH!>""<!>))
    foo(checkSubtype<Int>(<!ARGUMENT_TYPE_MISMATCH!>""<!>))
    foo(<!ARGUMENT_TYPE_MISMATCH!>checkSubtype<Long>(<!ARGUMENT_TYPE_MISMATCH!>""<!>)<!>)

    use(a, b, c, d)
}

fun foo(i: Int) = i

fun use(vararg a: Any?) = a
