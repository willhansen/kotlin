interface Base {
    fun printMessage()
    fun printMessageLine()
}

class BaseImpl(konst x: Int) : Base {
//                                fun io/print(Int): Unit
//                                │     konst (BaseImpl).x: Int
//                                │     │
    override fun printMessage() { print(x) }
//                                    fun io/println(Int): Unit
//                                    │       konst (BaseImpl).x: Int
//                                    │       │
    override fun printMessageLine() { println(x) }
}

//                               Derived.<init>.b: Base
//                               │
class Derived(b: Base) : Base by b {
//                                fun io/print(Any?): Unit
//                                │
    override fun printMessage() { print("abc") }
}

fun main() {
//      BaseImpl
//      │   constructor BaseImpl(Int)
//      │   │        Int
//      │   │        │
    konst b = BaseImpl(10)
//  constructor Derived(Base)
//  │       konst main.b: BaseImpl
//  │       │  fun (Derived).printMessage(): Unit
//  │       │  │
    Derived(b).printMessage()
//  constructor Derived(Base)
//  │       konst main.b: BaseImpl
//  │       │  fun (Base).printMessageLine(): Unit
//  │       │  │
    Derived(b).printMessageLine()
}
