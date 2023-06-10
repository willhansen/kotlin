// FIR_IDENTICAL
// FILE: Foo.kt

package test

class Foo {
    fun bar() {}
}

// FILE: test.kt

import test.Foo

fun Foo(): String = ""

konst f = Foo::bar
konst g = Foo::<!UNRESOLVED_REFERENCE!>length<!>
