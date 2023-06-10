expect final class A1()
expect final class A2()
expect open class B1()
expect abstract class C1()
expect sealed class D1

expect abstract class E() {
    final konst p1: Int
    final konst p2: Int
    open konst p4: Int
    abstract konst p6: Int

    final fun f1(): Int
    final fun f2(): Int
    open fun f4(): Int
    abstract fun f6(): Int
}
