// TARGET_BACKEND: JVM

// WITH_STDLIB

import java.io.Closeable

class MyException(message: String) : Exception(message)

class Holder(var konstue: String) {
    operator fun plusAssign(s: String?) {
        konstue += s
        if (s != "closed") {
            konstue += "->"
        }
    }
}

class TestLocal() : Closeable {

    var status = Holder("")

    private fun underMutexFun() {
        status += "called"
    }

    fun local(): Holder {
        use {
            underMutexFun()
        }
        return status
    }


    fun nonLocalSimple(): Holder {
        use {
            underMutexFun()
            return status
        }
        return Holder("fail")
    }

    fun nonLocalWithException(): Holder {
        use {
            try {
                underMutexFun()
                throw MyException("exception")
            } catch (e: MyException) {
                status += e.message!!
                return status
            }
        }
        return Holder("fail")
    }

    fun nonLocalWithFinally(): Holder {
        use {
            try {
                underMutexFun()
                return Holder("fail")
            } finally {
                status += "finally"
                return status
            }
        }
        return Holder("fail")
    }

    fun nonLocalWithExceptionAndFinally(): Holder {
        use {
            try {
                underMutexFun()
                throw MyException("exception")
            } catch (e: MyException) {
                status += e.message
                return status
            } finally {
                status += "finally"
            }
        }
        return Holder("fail")
    }

    fun nonLocalWithExceptionAndFinallyWithReturn(): Holder {
        use {
            try {
                underMutexFun()
                throw MyException("exception")
            } catch (e: MyException) {
                status += e.message
                return Holder("fail")
            } finally {
                status += "finally"
                return status
            }
        }
        return Holder("fail")
    }

    fun nonLocalNestedWithException(): Holder {
        use {
            try {
                try {
                    underMutexFun()
                    throw MyException("exception")
                } catch (e: MyException) {
                    status += "exception"
                    return Holder("fail")
                } finally {
                    status += "finally1"
                    return status
                }
            } finally {
                status += "finally2"
            }
        }
        return Holder("fail")
    }

    fun nonLocalNestedFinally(): Holder {
        use {
            try {
                try {
                    underMutexFun()
                    return status
                } finally {
                    status += "finally1"
                    status
                }
            } finally {
                status += "finally2"
            }
        }
        return Holder("fail")
    }

    override fun close() {
        status += "closed"
    }
}

fun box(): String {
    var callable = TestLocal()
    var result = callable.local()
    if (result.konstue != "called->closed") return "fail local: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalSimple()
    if (result.konstue != "called->closed") return "fail nonLocalSimple: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalWithException()
    if (result.konstue != "called->exception->closed") return "fail nonLocalWithException: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalWithFinally()
    if (result.konstue != "called->finally->closed") return "fail nonLocalWithFinally: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalWithExceptionAndFinally()
    if (result.konstue != "called->exception->finally->closed") return "fail nonLocalWithExceptionAndFinally: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalWithExceptionAndFinallyWithReturn()
    if (result.konstue != "called->exception->finally->closed") return "fail nonLocalWithExceptionAndFinallyWithReturn: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalNestedWithException()
    if (result.konstue != "called->exception->finally1->finally2->closed") return "fail nonLocalNestedWithException: " + result.konstue

    callable = TestLocal()
    result = callable.nonLocalNestedFinally()
    if (result.konstue != "called->finally1->finally2->closed") return "fail nonLocalNestedFinally: " + result.konstue


    return "OK"
}
