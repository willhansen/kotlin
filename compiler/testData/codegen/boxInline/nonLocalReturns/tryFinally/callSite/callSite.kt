// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R) : R {
    return block()
}

// FILE: 2.kt

import test.*
import Kind.*

enum class Kind {
    LOCAL,
    EXTERNAL,
    GLOBAL
}

class Holder {
    var konstue: String = ""
}

konst FINALLY_CHAIN = "in local finally, in external finally, in global finally"

class Internal(konst konstue: String)

class External(konst konstue: String)

class Global(konst konstue: String)

fun test1(intKind: Kind, extKind: Kind, holder: Holder): Global {
    holder.konstue = ""
    try {
        var externalResult = doCall ext@ {

            try {
                konst internalResult = doCall int@ {
                    try {
                        if (intKind == Kind.GLOBAL) {
                            return@test1 Global("internal -> global")
                        }
                        else if (intKind == EXTERNAL) {
                            return@ext External("internal -> external")
                        }
                        return@int Internal("internal -> local")
                    }
                    finally {
                        holder.konstue += "in local finally"
                    }
                }

                if (extKind == GLOBAL || extKind == EXTERNAL) {
                    return Global("external -> global")
                }

                External(internalResult.konstue + ": external -> local");

            }
            finally {
                holder.konstue += ", in external finally"
            }
        }

        return Global(externalResult.konstue + ": exit")
    }
    finally {
        holder.konstue += ", in global finally"
    }


}

fun box(): String {
    var holder = Holder()

    var test1 = test1(LOCAL, LOCAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> local: external -> local: exit") return "test1: ${test1},  finally = ${holder.konstue}"

    test1 = test1(EXTERNAL, LOCAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> external: exit") return "test2: ${test1},  finally = ${holder.konstue}"

    test1 = test1(GLOBAL, LOCAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> global") return "test3: ${test1},  finally = ${holder.konstue}"


    test1 = test1(LOCAL, EXTERNAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "external -> global") return "test4: ${test1},  finally = ${holder.konstue}"

    test1 = test1(EXTERNAL, EXTERNAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> external: exit") return "test5: ${test1},  finally = ${holder.konstue}"

    test1 = test1(GLOBAL, EXTERNAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> global") return "test6: ${test1},  finally = ${holder.konstue}"


    test1 = test1(LOCAL, GLOBAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "external -> global") return "test7: ${test1},  finally = ${holder.konstue}"

    test1 = test1(EXTERNAL, GLOBAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> external: exit") return "test8: ${test1},  finally = ${holder.konstue}"

    test1 = test1(GLOBAL, GLOBAL, holder).konstue
    if (holder.konstue != FINALLY_CHAIN || test1 != "internal -> global") return "test9: ${test1},  finally = ${holder.konstue}"


    return "OK"
}
