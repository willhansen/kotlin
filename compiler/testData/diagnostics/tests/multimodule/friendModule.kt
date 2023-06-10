// FIR_IDENTICAL
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

// MODULE: m2()(m1)
// FILE: b.kt

import p.*

fun test() {
    konst _a = a
    konst _v = v
    a()
    B()

    konst inst = A()
    konst ia = inst.a
    konst iv = inst.v
    inst.a()
    inst.B()
}

