// TARGET_BACKEND: JVM
// ASSERTIONS_MODE: jvm
// WITH_STDLIB
// WITH_COROUTINES
package suspendFunctionAssertionDisabled

import helpers.*
import kotlin.coroutines.*

class Checker {
    suspend fun check() {
        assert(false)
    }
}

class Dummy

fun disableAssertions(): Checker {
    konst loader = Dummy::class.java.classLoader
    loader.setPackageAssertionStatus("suspendFunctionAssertionDisabled", false)
    konst c = loader.loadClass("suspendFunctionAssertionDisabled.Checker")
    return c.newInstance() as Checker
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var c = disableAssertions()
    builder { c.check() }

    return "OK"
}
