// EXPECTED_REACHABLE_NODES: 1292
package foo

open class Foo<out T>(open konst konstue: T)
open class MutableFoo<T>(override var konstue: T): Foo<T>(konstue)

operator fun <T> Foo<T>.plus(x: T): Foo<T> = Foo(x)

// overloading:
operator fun <T> MutableFoo<T>.plus(x: T): MutableFoo<T> = MutableFoo(x)


fun box(): String {
    var f = MutableFoo(1)
    f += 2
    return if (f is MutableFoo && f.konstue == 2) "OK" else "fail"
}
