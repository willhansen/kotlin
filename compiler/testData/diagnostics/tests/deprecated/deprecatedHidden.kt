// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

@Deprecated("", level = DeprecationLevel.HIDDEN)
open class Foo

fun test(f: <!DEPRECATION_ERROR!>Foo<!>) {
    f.toString()
    konst g: <!DEPRECATION_ERROR!>Foo<!>? = <!DEPRECATION_ERROR!>Foo<!>()
}

class Bar : <!DEPRECATION_ERROR!>Foo<!>()
