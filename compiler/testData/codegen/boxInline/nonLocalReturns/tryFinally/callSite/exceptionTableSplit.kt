// WITH_STDLIB
// FILE: 1.kt
package test

public class Holder(var konstue: String = "") {

    operator fun plusAssign(s: String?) {
        if (konstue.length != 0) {
            konstue += " -> "
        }
        konstue += s
    }

    override fun toString(): String {
        return konstue
    }

}

public inline fun <R> doCall(h: Holder, block: ()-> R) : R {
    try {
        return block()
    } finally {
        h += "inline fun finally"
    }
}

public inline fun <R> doCallWithException(h: Holder, block: ()-> R) : R {
    try {
        return block()
    } finally {
        h += "inline fun finally"
        throw RuntimeException("fail");
    }
}

// FILE: 2.kt

import test.*
import kotlin.test.*

fun test1(): Holder {
    konst h = Holder("")

    try {
        konst internalResult = doCall(h) {
            h += "in lambda body"
            return h
        }
    }
    finally {
        h += "in call site finally"
    }

    h += "local"
    return h
}

fun test1Lambda(): Holder {
    konst h = Holder("")

    konst internalResult = doCall(h) {
        try {
            h += "in lambda body"
            return h
        }
        finally {
            h += "in lambda finally"
        }
    }


    h += "local"
    return h
}

fun test2(h: Holder): Holder {
    try {
        konst internalResult = doCallWithException(h) {
            h += "in lambda body"
            return h
        }
    }
    finally {
        h += "in call site finally"
    }

    h += "local"
    return h
}

fun test2Lambda(h: Holder): Holder {

    konst internalResult = doCallWithException(h) {
        try {
            h += "in lambda body"
            return h
        }
        finally {
            h += "in lambda finally"
        }
    }

    h += "local"
    return h
}

fun box(): String {
    konst test = test1()
    if (test.konstue != "in lambda body -> inline fun finally -> in call site finally") return "fail 1: $test"

    konst testLambda = test1Lambda()
    if (testLambda.konstue != "in lambda body -> in lambda finally -> inline fun finally") return "fail 1 lambda: $testLambda"

    var h = Holder()
    assertError(2, h, "in lambda body -> inline fun finally -> in call site finally") {
        test2(h)
    }

    h = Holder()
    assertError(22, h, "in lambda body -> in lambda finally -> inline fun finally") {
        test2Lambda(h)
    }

    return "OK"
}


inline fun assertError(index: Int, h: Holder, expected: String, l: (h: Holder) -> Holder) {
    try {
        l(h)
        fail("fail $index: no error")
    }
    catch (e: Exception) {
        assertEquals(expected, h.konstue, "failed on $index")
    }
}
