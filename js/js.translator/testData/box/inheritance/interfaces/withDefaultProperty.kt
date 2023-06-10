// EXPECTED_REACHABLE_NODES: 1288
interface I {
    konst foo: String
        get() = "OK"
}

class A : I

fun box() = A().foo