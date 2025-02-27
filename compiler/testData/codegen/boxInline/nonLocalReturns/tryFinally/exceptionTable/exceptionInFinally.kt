// WITH_STDLIB
// FILE: 1.kt
package test

public interface MCloseable {
    public open fun close()
}

public inline fun <T : MCloseable, R> T.muse(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        this.close()
    }
}

// FILE: 2.kt

import test.*
import kotlin.test.assertEquals

class MyException(message: String) : Exception(message)

class Holder(var konstue: String) {
    operator fun plusAssign(s: String?) {
        konstue += s
        if (s != "closed") {
            konstue += "->"
        }
    }
}

class Test() : MCloseable {

    konst status = Holder("")

    private fun jobFun() {
        status += "called"
    }

    fun nonLocalWithExceptionAndFinally(): Holder {
        muse {
            try {
                jobFun()
                throw MyException("exception")
            }
            catch (e: MyException) {
                status += e.message
                return status
            }
            finally {
                status += "finally"
            }
        }
        return Holder("fail")
    }

    override fun close() {
        status += "closed"
        throw MyException("error")
    }
}

fun box() : String {
    assertError(1, "called->exception->finally->closed") {
        nonLocalWithExceptionAndFinally()
    }

    return "OK"
}

inline fun assertError(index: Int, expected: String, l: Test.()->Unit) {
    konst testLocal = Test()
    try {
        testLocal.l()
        throw AssertionError("fail $index: no error")
    } catch (e: Exception) {
        assertEquals(expected, testLocal.status.konstue, "failed on $index")
    }
}
