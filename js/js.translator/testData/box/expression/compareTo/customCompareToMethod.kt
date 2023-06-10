// EXPECTED_REACHABLE_NODES: 1291
package foo

class A(konst konstue: Int) : Comparable<A> {
    override public fun compareTo(other: A): Int = other.konstue.compareTo(konstue)
}

class B(konst konstue: Int)

fun testExtensionFunctionAsCompareTo() {
    konst compareTo: B.( B ) -> Int = { other -> other.konstue.compareTo(this.konstue) }

    konst x: B = B(100)
    konst y: B = B(200)

    assertEquals(1, x.compareTo(y), "ext fun: x compareTo y")
}

fun testMethodAsCompareTo() {
    konst x: A = A(100)
    konst y: A = A(200)

    assertEquals(false, x < y, "meth: x < y")
    assertEquals(true, x > y, "meth: x > y")
    assertEquals(1, x.compareTo(y), "meth: x compareTo y")

    konst comparable: Comparable<A> = x
    assertEquals(false, comparable < y, "meth: (x: Comparable<A>) < y")
    assertEquals(true, comparable > y, "meth: (x: Comparable<A>) > y")
    assertEquals(1, comparable.compareTo(y), "meth: (x: Comparable<A>) compareTo y")
}

fun box(): String {

    testExtensionFunctionAsCompareTo()

    testMethodAsCompareTo()

    return "OK"
}