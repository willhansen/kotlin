// EXPECTED_REACHABLE_NODES: 1292

var result = ""
abstract class Parent {
    konst o = "O"
    konst k = "K"
    protected fun getO() = o
    protected fun getK() = k
}

class Child : Parent() {
    private fun calculateResult(): String {
        return super.getO() + super.getK()
    }
    fun runTest() {
        result += this.calculateResult()
    }
}

fun box(): String {
    Child().runTest()
    return result
}
