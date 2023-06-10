// EXPECTED_REACHABLE_NODES: 1292

var result = ""

fun <T> call(lambda: () -> T): T {
    return lambda()
}

abstract class Parent {
    konst o = "O"
    konst k = "K"
    protected fun getO() = o
    protected fun getK() = k
}

class Child : Parent() {
    fun runTest() {
        result += call { super.getO() + super.getK() }
    }
}

fun box(): String {
    Child().runTest()
    return result
}
