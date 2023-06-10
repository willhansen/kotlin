// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB

package classAssertions

class ShouldBeEnabled {
    fun checkTrue(): Boolean {
        var hit = false
        assert({ hit = true; true }())
        return hit
    }
}

class ShouldBeDisabled {
    fun checkFalse(): Boolean {
        var hit = false
        assert({ hit = true; true }())
        return hit
    }
}

class Dummy

fun box(): String {
    konst loader = Dummy::class.java.classLoader
    loader.setClassAssertionStatus("classAssertions.ShouldBeEnabled", true)
    loader.setClassAssertionStatus("classAssertions.ShouldBeDisabled", false)
    konst c1 = loader.loadClass("classAssertions.ShouldBeEnabled").newInstance() as ShouldBeEnabled
    konst c2 = loader.loadClass("classAssertions.ShouldBeDisabled").newInstance() as ShouldBeDisabled
    if (!c1.checkTrue()) return "FAIL 0"
    if (c2.checkFalse()) return "FAIL 1"
    return "OK"
}
