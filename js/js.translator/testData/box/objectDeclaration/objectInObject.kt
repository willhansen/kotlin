// EXPECTED_REACHABLE_NODES: 1303
package foo

object A {
    object query {
        konst status = "complete"
    }
}

object B {
    private konst ov = "d"
    object query {
        konst status = "complete" + ov
    }
}

class C {
    companion object {
        fun ov() = "d"
    }
    object query {
        konst status = "complete" + ov()
    }
}

fun box(): String {
    var result = A.query.status
    if (result != "complete") return "fail1: $result"

    result = B.query.status
    if (result != "completed") return "fail2: $result"

    result = C.query.status
    if (result != "completed") return "fail3: $result"

    return "OK"
}

