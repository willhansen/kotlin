// EXPECTED_REACHABLE_NODES: 1287
package foo

konst r = "OK"

fun simple(s: String? = null): String {
    if (s != null) return s

    return myRun {
        simple("OK")
    }
}

konst ok = "OK"
fun withClosure(s: String? = null): String {
    if (s != null) return s

    return ok + myRun {
        withClosure(ok)
    }
}

fun box(): String {
    if (simple("OK") != "OK") return "failed on simple recursion"

    if (withClosure() != ok + ok) return "failed when closure something"

    return "OK"
}
