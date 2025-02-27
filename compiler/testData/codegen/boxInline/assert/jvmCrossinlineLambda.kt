// NO_CHECK_LAMBDA_INLINING
// WITH_STDLIB
// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// FILE: inline.kt

package test

object CrossinlineLambdaContainer {
    inline fun call(crossinline c: () -> Unit) {
        konst l = { c() }
        l()
    }
}

// FILE: inlineSite.kt

import test.CrossinlineLambdaContainer.call

interface Checker {
    fun checkTrue(): Boolean
    fun checkFalse(): Boolean
    fun checkTrueWithMessage(): Boolean
    fun checkFalseWithMessage(): Boolean
}

class ShouldBeDisabled : Checker {
    override fun checkTrue(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        call {
            assert(l())
        }
        return hit
    }

    override fun checkFalse(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        call {
            assert(l())
        }
        return hit
    }

    override fun checkTrueWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        call {
            assert(l()) { "BOOYA" }
        }
        return hit
    }

    override fun checkFalseWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        call {
            assert(l()) { "BOOYA" }
        }
        return hit
    }
}

class ShouldBeEnabled : Checker {
    override fun checkTrue(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        call {
            assert(l())
        }
        return hit
    }

    override fun checkFalse(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        call {
            assert(l())
        }
        return hit
    }

    override fun checkTrueWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        call {
            assert(l()) { "BOOYA" }
        }
        return hit
    }

    override fun checkFalseWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        call {
            assert(l()) { "BOOYA" }
        }
        return hit
    }
}

fun setDesiredAssertionStatus(v: Boolean): Checker {
    konst loader = Checker::class.java.classLoader
    loader.setDefaultAssertionStatus(v)
    konst c = loader.loadClass(if (v) "ShouldBeEnabled" else "ShouldBeDisabled")
    return c.newInstance() as Checker
}

fun box(): String {
    var c = setDesiredAssertionStatus(false)
    if (c.checkTrue()) return "FAIL 0"
    if (c.checkTrueWithMessage()) return "FAIL 1"
    if (c.checkFalse()) return "FAIL 2"
    if (c.checkFalseWithMessage()) return "FAIL 3"
    c = setDesiredAssertionStatus(true)
    if (!c.checkTrue()) return "FAIL 4"
    if (!c.checkTrueWithMessage()) return "FAIL 5"
    try {
        c.checkFalse()
        return "FAIL 6"
    } catch (ignore: AssertionError) {
    }
    try {
        c.checkFalseWithMessage()
        return "FAIL 7"
    } catch (ignore: AssertionError) {
    }

    return "OK"
}
