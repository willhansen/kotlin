// !DIAGNOSTICS: -UNUSED_VARIABLE
// MODULE: m1
// FILE: a.kt

package p

public class A {
    private konst a = A()
    private var v = A()
    private fun a() = A()
    private inner class B
}

private konst a = A()
private var v = A()
private fun a() = A()
private class B

// MODULE: m2()(m1)
// FILE: b.kt

import p.*

fun test() {
    konst _a = <!INVISIBLE_REFERENCE!>a<!>
    konst _v = <!INVISIBLE_REFERENCE!>v<!>
    <!INVISIBLE_REFERENCE!>a<!>()
    <!INVISIBLE_REFERENCE!>B<!>()

    konst inst = A()
    konst ia = inst.<!INVISIBLE_REFERENCE!>a<!>
    konst iv = inst.<!INVISIBLE_REFERENCE!>v<!>
    inst.<!INVISIBLE_REFERENCE!>a<!>()
    inst.<!INVISIBLE_REFERENCE!>B<!>()
}
