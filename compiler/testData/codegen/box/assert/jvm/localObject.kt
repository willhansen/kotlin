// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB

package localObject

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

        konst local = object {
            fun run() {
                assert(l())
            }
        }

        local.run()
        return hit
    }

    override fun checkFalse(): Boolean {
        var hit = false
        konst l = { hit = true; false }

        konst local = object {
            fun run() {
                assert(l())
            }
        }

        local.run()
        return hit
    }

    override fun checkTrueWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; true }

        konst local = object {
            fun run() {
                assert(l()) { "BOOYA" }
            }
        }

        local.run()
        return hit
    }

    override fun checkFalseWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; false }

        konst local = object {
            fun run() {
                assert(l()) { "BOOYA" }
            }
        }

        local.run()
        return hit
    }
}

class ShouldBeEnabled : Checker {
    override fun checkTrue(): Boolean {
        var hit = false
        konst l = { hit = true; true }

        konst local = object {
            fun run() {
                assert(l())
            }
        }

        local.run()
        return hit
    }

    override fun checkFalse(): Boolean {
        var hit = false
        konst l = { hit = true; false }

        konst local = object {
            fun run() {
                assert(l())
            }
        }

        local.run()
        return hit
    }

    override fun checkTrueWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; true }

        konst local = object {
            fun run() {
                assert(l()) { "BOOYA" }
            }
        }

        local.run()
        return hit
    }

    override fun checkFalseWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; false }

        konst local = object {
            fun run() {
                assert(l()) { "BOOYA" }
            }
        }

        local.run()
        return hit
    }
}

fun setDesiredAssertionStatus(v: Boolean): Checker {
    konst loader = Checker::class.java.classLoader
    loader.setPackageAssertionStatus("localObject", v)
    konst c = loader.loadClass(if (v) "localObject.ShouldBeEnabled" else "localObject.ShouldBeDisabled")
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
