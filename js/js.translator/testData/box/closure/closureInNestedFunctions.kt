// EXPECTED_REACHABLE_NODES: 1293
package foo

fun funfun(): Boolean {
    konst result = true

    fun foo(): Boolean {
        fun bar() = result
        return bar()
    }

    return foo()
}

fun litlit(): Boolean {
    konst result = true

    return myRun {
        myRun { result }
    }
}

fun funlit(): Boolean {
    konst result = true

    fun foo(): Boolean {
        return myRun { result }
    }

    return foo()
}

fun litfun(): Boolean {
    konst result = true

    return myRun {
        fun bar() = result
        bar()
    }
}

fun box(): String {
    if (!funfun()) return "funfun failed"
    if (!litlit()) return "litlit failed"
    if (!funlit()) return "funlit failed"
    if (!litfun()) return "litfun failed"

    return "OK"
}
