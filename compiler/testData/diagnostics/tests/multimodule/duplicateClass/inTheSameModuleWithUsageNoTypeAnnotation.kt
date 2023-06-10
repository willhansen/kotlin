// MODULE: m1
// FILE: a.kt

package p

public class A
public class B {
    public konst a: A = A()
}

// MODULE: m2(m1)
// FILE: b.kt

package p

class A {
    fun foo() {}
}

fun test() {
    konst a = B().a
    a.<!UNRESOLVED_REFERENCE!>foo<!>()
}