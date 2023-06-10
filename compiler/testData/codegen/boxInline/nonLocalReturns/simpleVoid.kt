// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R) : R {
    return block()
}

// FILE: 2.kt

import test.*

class Holder(var konstue: Int)

fun test1(holder: Holder, doNonLocal: Boolean) {
    holder.konstue = -1;

    konst localResult = doCall {
        if (doNonLocal) {
            holder.konstue = 1000
            return
        }
        10
    }

    holder.konstue = localResult
}


fun box(): String {
    konst h = Holder(-1)

    test1(h, false)
    if (h.konstue != 10) return "test1: ${h.konstue}"

    test1(h, true)
    if (h.konstue != 1000) return "test2: ${h.konstue}"

    return "OK"
}
