// !DIAGNOSTICS: -UNUSED_VARIABLE -NOTHING_TO_INLINE
// MODULE: m1
// FILE: a.kt

package p

public class A {
    @PublishedApi
    internal konst a = A()
    @PublishedApi
    internal var v = A()
    @PublishedApi
    internal fun a() = A()
    @PublishedApi
    internal inner class B
}

@PublishedApi
internal konst a = A()
@PublishedApi
internal var v = A()
@PublishedApi
internal fun a() = A()
@PublishedApi
internal class B

// MODULE: m2(m1)
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

inline fun testInline() {
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
