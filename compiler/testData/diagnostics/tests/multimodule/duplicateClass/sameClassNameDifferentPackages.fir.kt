// !DIAGNOSTICS: -UNUSED_VARIABLE
// MODULE: m1
// FILE: a.kt

package p

public class A
public class B {
    public konst a: A = A()
}

// MODULE: m2(m1)
// FILE: b.kt

import p.*

class A

fun test() {
    konst a: A = <!INITIALIZER_TYPE_MISMATCH!>B().a<!>
}