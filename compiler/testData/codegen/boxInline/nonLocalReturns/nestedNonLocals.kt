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

class Internal(konst konstue: String)

class External(konst konstue: String)

class Global(konst konstue: String)

fun test1(intKind: Kind, extKind: Kind): Global {

    var externalResult = doCall ext@ {

        konst internalResult = doCall int@ {
            if (intKind == Kind.GLOBAL) {
                return@test1 Global("internal -> global")
            } else if (intKind == EXTERNAL) {
                return@ext External("internal -> external")
            }
            return@int Internal("internal -> local")
        }

        if (extKind == GLOBAL || extKind == EXTERNAL) {
            return Global("external -> global")
        }

        External(internalResult.konstue + ": external -> local");
    }

    return Global(externalResult.konstue + ": exit")
}

fun box(): String {
    var test1 = test1(LOCAL, LOCAL).konstue
    if (test1 != "internal -> local: external -> local: exit") return "test1: ${test1}"

    test1 = test1(EXTERNAL, LOCAL).konstue
    if (test1 != "internal -> external: exit") return "test2: ${test1}"

    test1 = test1(GLOBAL, LOCAL).konstue
    if (test1 != "internal -> global") return "test3: ${test1}"


    test1 = test1(LOCAL, EXTERNAL).konstue
    if (test1 != "external -> global") return "test4: ${test1}"

    test1 = test1(EXTERNAL, EXTERNAL).konstue
    if (test1 != "internal -> external: exit") return "test5: ${test1}"

    test1 = test1(GLOBAL, EXTERNAL).konstue
    if (test1 != "internal -> global") return "test6: ${test1}"


    test1 = test1(LOCAL, GLOBAL).konstue
    if (test1 != "external -> global") return "test7: ${test1}"

    test1 = test1(EXTERNAL, GLOBAL).konstue
    if (test1 != "internal -> external: exit") return "test8: ${test1}"

    test1 = test1(GLOBAL, GLOBAL).konstue
    if (test1 != "internal -> global") return "test9: ${test1}"


    return "OK"
}
