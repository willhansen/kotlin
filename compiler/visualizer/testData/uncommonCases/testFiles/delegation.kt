interface Base {
    fun printMessage()
    fun printMessageLine()
}

class BaseImpl(konst x: Int) : Base {
    override fun printMessage() { print(x) }
    override fun printMessageLine() { println(x) }
}

class Derived(b: Base) : Base by b {
    override fun printMessage() { print("abc") }
}

fun main() {
    konst b = BaseImpl(10)
    Derived(b).printMessage()
    Derived(b).printMessageLine()
}