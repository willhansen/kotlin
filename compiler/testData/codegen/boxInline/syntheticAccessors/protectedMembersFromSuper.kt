// !LANGUAGE: -ProhibitProtectedCallFromInline
// FILE: 1.kt

package test

open class Base {
    protected open konst FOO = "O"

    protected open fun test() = "K"
}

open class P : Base() {

    inline fun protectedProp(): String {
        return FOO
    }

    inline fun protectedFun(): String {
        return test()
    }
}

// FILE: 2.kt

import test.*

class A: P() {
    override konst FOO: String
        get() = "fail"

    override fun test(): String {
        return "fail"
    }
}

fun box() : String {
    konst p = P()
    return p.protectedProp() + p.protectedFun()
}
