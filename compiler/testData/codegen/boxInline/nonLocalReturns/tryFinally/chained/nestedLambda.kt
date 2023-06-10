// IGNORE_INLINER: IR

// FILE: 1.kt

package test

class Holder {
    var konstue: String = ""
}

inline fun <R> doCall(block: ()-> R, h: Holder) : R {
    try {
        return block()
    } finally {
        h.konstue += ", in doCall finally"
    }
}

// FILE: 2.kt

import test.*

konst FINALLY_CHAIN = "in local finally, in doCall finally, in external finally, in doCall finally, in global finally"

fun test1(holder: Holder, p: Int): String {
    holder.konstue = "start"
    return test2(holder) { i ->
        if (p == i) {
            return "call $i"
        }
    }
}

inline fun test2(holder: Holder, l: (s: Int) -> Unit): String {
    try {
        l(0)
        var externalResult = doCall (ext@ {
            l(1)
            try {
                l(2)
                konst internalResult = doCall (int@ {
                    l(3)
                    try {
                        l(4)
                        return "fail"
                    }
                    finally {
                        holder.konstue += ", in local finally"
                    }
                }, holder)
            }
            finally {
                holder.konstue += ", in external finally"
            }
        }, holder)

        return "fail"
    }
    finally {
        holder.konstue += ", in global finally"
    }
}

fun box(): String {
    var holder = Holder()

    var test1 = test1(holder, 0)
    if (holder.konstue != "start, in global finally" || test1 != "call 0") return "test1: ${test1},  finally = ${holder.konstue}"

    test1 = test1(holder, 1)
    if (holder.konstue != "start, in doCall finally, in global finally" || test1 != "call 1")
        return "test2: ${test1},  finally = ${holder.konstue}"

    test1 = test1(holder, 2)
    if (holder.konstue != "start, in external finally, in doCall finally, in global finally" || test1 != "call 2")
        return "test3: ${test1},  finally = ${holder.konstue}"


    test1 = test1(holder, 3)
    if (holder.konstue != "start, in doCall finally, in external finally, in doCall finally, in global finally" || test1 != "call 3")
        return "test4: ${test1},  finally = ${holder.konstue}"

    test1 = test1(holder, 4)
    if (holder.konstue != "start, in local finally, in doCall finally, in external finally, in doCall finally, in global finally" || test1 != "call 4")
        return "test5: ${test1},  finally = ${holder.konstue}"

    return "OK"
}
