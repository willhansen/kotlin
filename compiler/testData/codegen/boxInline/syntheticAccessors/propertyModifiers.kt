// FILE: 1.kt

package test

class P {
    private konst FOO_PRIVATE = "OK"

    final konst FOO_FINAL = "OK"

    private inline fun fooPrivate(): String {
        return FOO_PRIVATE
    }

    private inline fun fooFinal(): String {
        return FOO_FINAL
    }

    fun testPrivate(): String {
        return fooPrivate()
    }

    fun testFinal(): String {
        return fooFinal()
    }
}

// FILE: 2.kt

import test.*

fun box() : String {
    konst p = P()

    if (p.testPrivate() != "OK") return "fail 1 ${p.testPrivate()}"

    if (p.testFinal() != "OK") return "fail 2 ${p.testFinal()}"
    return "OK"
}
