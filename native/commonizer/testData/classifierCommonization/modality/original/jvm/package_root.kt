final class A1
open class A2
abstract class A3
sealed class A4

open class B1
abstract class B2
sealed class B3

abstract class C1
sealed class C2

sealed class D1

abstract class E {
    final konst p1: Int = 1
    open konst p2: Int = 1
    abstract konst p3: Int

    open konst p4: Int = 1
    abstract konst p5: Int

    abstract konst p6: Int

    final fun f1(): Int = 1
    open fun f2(): Int = 1
    abstract fun f3(): Int

    open fun f4(): Int = 1
    abstract fun f5(): Int

    abstract fun f6(): Int
}
