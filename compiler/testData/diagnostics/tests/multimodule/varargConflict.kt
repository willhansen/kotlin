// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// MODULE: m1
// FILE: a.kt

package p

public fun foo(a: Int) {}
public fun foo(vararg konstues: Int) {}

// MODULE: m2
// FILE: b.kt

package p

public fun foo(a: Int) {}
public fun foo(vararg konstues: Int) {}

// MODULE: m3(m1, m2)
// FILE: c.kt
package m

import p.foo

fun main() {
    foo(12)
}