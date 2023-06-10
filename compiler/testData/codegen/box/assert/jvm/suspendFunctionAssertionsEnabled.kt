// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB
// WITH_COROUTINES
package suspendFunctionAssertionsEnabled

import helpers.*
import kotlin.coroutines.*

class Checker {
    suspend fun check() {
        assert(false)
    }
}

class Dummy

fun enableAssertions(): Checker {
    konst loader = Dummy::class.java.classLoader
    loader.setPackageAssertionStatus("suspendFunctionAssertionsEnabled", true)
    konst c = loader.loadClass("suspendFunctionAssertionsEnabled.Checker")
    return c.newInstance() as Checker
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var c = enableAssertions()
    try {
        builder { c.check() }
        return "FAIL 6"
    } catch (ignore: AssertionError) {
    }

    return "OK"
}
