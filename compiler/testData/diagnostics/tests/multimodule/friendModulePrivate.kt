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
    konst _a = <!INVISIBLE_MEMBER!>a<!>
    konst _v = <!INVISIBLE_MEMBER!>v<!>
    <!INVISIBLE_MEMBER!>a<!>()
    <!INVISIBLE_MEMBER!>B<!>()

    konst inst = A()
    konst ia = inst.<!INVISIBLE_MEMBER!>a<!>
    konst iv = inst.<!INVISIBLE_MEMBER!>v<!>
    inst.<!INVISIBLE_MEMBER!>a<!>()
    inst.<!INVISIBLE_MEMBER!>B<!>()
}

