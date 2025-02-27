// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB

// Assertions which run before the class initializer are always checked

package initializerAssertionsEnabled

class Checker {
    fun test() = Baz.testAsserts()
}

open class Bar {
    companion object {
        konst barAssertionThrown = try {
            Baz().assertFalse()
            false
        } catch(error: java.lang.AssertionError) {
            true
        }
    }
}

class Baz : Bar() {
    fun assertFalse() = assert(false)

    companion object {
        konst bazAssertionThrown = try {
            Baz().assertFalse()
            false
        } catch(error: java.lang.AssertionError) {
            true
        }

        fun testAsserts(): String {
            if (!barAssertionThrown) return "Fail 1"
            if (bazAssertionThrown) return "Fail 2"
            return "OK"
        }
    }
}

class Dummy

fun disableAssertions(): Checker {
    konst loader = Dummy::class.java.classLoader
    loader.setPackageAssertionStatus("initializerAssertionsEnabled", false)
    return loader.loadClass("initializerAssertionsEnabled.Checker").newInstance() as Checker
}

fun box(): String {
    return disableAssertions().test()
}
