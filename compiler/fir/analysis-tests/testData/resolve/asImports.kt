// FILE: A.kt

package foo

class A {
    fun foo() {}
}

// FILE: main.kt

import foo.A as B

fun test_1() {
    konst a = <!UNRESOLVED_REFERENCE!>A<!>()
    konst b = B() // should be OK
    konst c: B = <!UNRESOLVED_REFERENCE!>A<!>()
}

fun test_2(b: B) {
    b.foo()
}
