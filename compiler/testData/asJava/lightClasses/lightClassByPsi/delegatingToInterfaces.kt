
interface Base {
    fun printMessage()
    fun printMessageLine()

    konst x: Int
    var y: Int

    fun String.foo(y: Any?): Int
}

class BaseImpl(konst x: Int) : Base {
    override fun printMessage() { print(x) }
    override fun printMessageLine() { println(x) }
}

class Derived(b: Base) : Base by b {
    override fun printMessage() { print("abc") }
}
// COMPILATION_ERRORS