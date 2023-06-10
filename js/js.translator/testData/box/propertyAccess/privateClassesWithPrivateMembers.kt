// EXPECTED_REACHABLE_NODES: 1305

private class A {
    private konst f = "OK"
    inline fun ii() = f
}


private class B {
    private konst a = A()
    fun foo() = a.ii()
}

fun box() = B().foo()