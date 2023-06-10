// !DIAGNOSTICS: -UNUSED_VARIABLE
// MODULE: m1
// FILE: a.kt

package p

public class A {
    internal konst a = A()
    internal var v = A()
    internal fun a() = A()
    internal inner class B
}

internal konst a = A()
internal var v = A()
internal fun a() = A()
internal class B

// MODULE: m2(m1)
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
