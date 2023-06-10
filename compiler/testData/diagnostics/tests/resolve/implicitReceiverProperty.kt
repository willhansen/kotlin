// FIR_IDENTICAL
// FILE: a.kt
package a

class A(konst b: String) {
    companion object {
        konst c: String = ""
    }

    fun mtd() = c.length
}

// FILE: b.kt
package b

// FILE: c.kt
package c

// FILE: test.kt
package test

import a.A

fun <T, R> T.with(f: T.() -> R) = f()

fun A.extFun1() = b.length

// fun A.extFun2() = c.length // TODO fix KT-9953

konst x1 = A("").with { b.length }

// konst x2 = A("").with { c.length } // TODO fix KT-9953

konst x3 = A.with { c.length }