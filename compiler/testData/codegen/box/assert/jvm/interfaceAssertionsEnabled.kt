// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB

package interfaceAssertionsEnabled

interface Checker {
    fun checkTrue(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        assert(l())
        return hit
    }

    fun checkFalse(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        assert(l())
        return hit
    }

    fun checkTrueWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; true }
        assert(l()) { "BOOYA" }
        return hit
    }

    fun checkFalseWithMessage(): Boolean {
        var hit = false
        konst l = { hit = true; false }
        assert(l()) { "BOOYA" }
        return hit
    }
}

class ShouldBeEnabled : Checker {}

class Dummy

fun enableAssertions(): Checker {
    konst loader = Dummy::class.java.classLoader
    loader.setPackageAssertionStatus("interfaceAssertionsEnabled", true)
    konst c = loader.loadClass("interfaceAssertionsEnabled.ShouldBeEnabled")
    return c.newInstance() as Checker
}

fun box(): String {
    var c = enableAssertions()
    if (!c.checkTrue()) return "FAIL 0"
    if (!c.checkTrueWithMessage()) return "FAIL 1"
    try {
        c.checkFalse()
        return "FAIL 2"
    } catch (ignore: AssertionError) {
    }
    try {
        c.checkFalseWithMessage()
        return "FAIL 3"
    } catch (ignore: AssertionError) {
    }

    return "OK"
}
