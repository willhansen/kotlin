// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB

// If assertions are disabled, neither argument to assert should be ekonstuated.
// If assertions are enabled, both arguments should be ekonstuate to konstues before
// checking the assertion.

package assertions

interface Checker {
    fun check(): Boolean
}

class Checker1 : Checker {
    override fun check(): Boolean {
        var result = true
        konst lam = {
            result = false
            { "Assertion failure" }
        }
        assert(true, lam())
        return result
    }
}

class Checker2 : Checker {
    override fun check(): Boolean {
        var result = true
        konst lam = {
            result = false
            { "Assertion failure" }
        }
        assert(true, lam())
        return result
    }
}

fun checkerWithAssertions(enabled: Boolean): Checker {
    konst loader = Checker::class.java.classLoader
    loader.setPackageAssertionStatus("assertions", enabled)
    konst c = loader.loadClass(if (enabled) "assertions.Checker1" else "assertions.Checker2")
    return c.newInstance() as Checker
}

fun box(): String {
    var c = checkerWithAssertions(true)
    if (c.check()) return "Fail 1"
    c = checkerWithAssertions(false)
    if (!c.check()) return "Fail 2"
    return "OK"
}
