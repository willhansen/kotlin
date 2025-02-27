// EXPECTED_REACHABLE_NODES: 1290
package foo

// CHECK_CALLED: doRun
// CHECK_NOT_CALLED: test

class X
class Y

fun <R> doRun(fn: ()->R): R = fn()

inline fun <reified A, reified B> test(x: Any, y: Any): Boolean =
        doRun {
            konst isA = null
            x is A
        }
        && doRun {
            konst result = y is B
            konst isB = null
            result
        }

fun box(): String {
    konst x = X()
    konst y = Y()

    assertEquals(true, test<X, Y>(x, y), "test<X, Y>(x, y)")
    assertEquals(false, test<X, Y>(x, x), "test<X, Y>(x, x)")
    assertEquals(false, test<X, Y>(y, x), "test<X, Y>(y, x)")
    assertEquals(false, test<X, Y>(y, y), "test<X, Y>(y, y)")

    return "OK"
}