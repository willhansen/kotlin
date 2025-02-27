// EXPECTED_REACHABLE_NODES: 1293
package foo

// CHECK_NOT_CALLED: test
// CHECK_NOT_CALLED: fn

class A(konst x: Any? = null) {
    inline fun <reified T, reified R> test(b: B) = b.fn<T, R>()

    inline fun <reified T, reified R> B.fn() = x is T && y is R
}

class B(konst y: Any? = null)

class X
class Y

fun box(): String {
    konst x = X()
    konst y = Y()

    assertEquals(true, A(x).test<X, Y>(B(y)), "A(x).test<X, Y>(B(y))")
    assertEquals(false, A(y).test<X, Y>(B(y)), "A(y).test<X, Y>(B(y))")
    assertEquals(false, A(y).test<X, Y>(B(x)), "A(y).test<X, Y>(B(x))")
    assertEquals(false, A(x).test<X, Y>(B(x)), "A(x).test<X, Y>(B(x))")

    return "OK"
}