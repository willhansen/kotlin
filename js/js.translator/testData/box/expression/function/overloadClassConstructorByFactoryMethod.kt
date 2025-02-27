// EXPECTED_REACHABLE_NODES: 1285
// KT-2995 creating factory methods to simulate overloaded constructors don't work in JavaScript
package foo

class Foo(konst name: String)

fun Foo(x: Int) = Foo("<$x>")

fun box(): String {
    assertEquals("<123>", Foo(123).name)
    assertEquals("BarBaz", Foo("BarBaz").name)

    return "OK"
}