// EXPECTED_REACHABLE_NODES: 1287
class A

class B {
    konst A.x: String
        get() = "OK"

    fun result(a: A) = a.x
}

fun box() = B().result(A())
