// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun foo(x: Int?) {}
fun foo(y: String?) {}
fun foo(z: Boolean) {}

fun <T> baz(element: (T) -> Unit): T? = null

fun test1() {
    konst a1: Int? = baz(::foo)
    konst a2: String? = baz(::foo)
    konst a3: Boolean? = baz<Boolean>(::foo)

    baz<Int>(::foo).checkType { _<Int?>() }
    baz<String>(::foo).checkType { _<String?>() }
    baz<Boolean>(::foo).checkType { _<Boolean?>() }

    konst b1: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>baz(::<!UNRESOLVED_REFERENCE!>foo<!>)<!>
    konst b2: String = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>baz(::<!UNRESOLVED_REFERENCE!>foo<!>)<!>
    konst b3: Boolean = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>baz(::<!UNRESOLVED_REFERENCE!>foo<!>)<!>
}
