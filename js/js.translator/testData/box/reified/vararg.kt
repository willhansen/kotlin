// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1682
package foo

// CHECK_NOT_CALLED_IN_SCOPE: function=test scope=box

class A(konst x: Int)
class B(konst x: Int)

inline fun <reified T> test(vararg xs: Any): List<T> {
    konst ts = arrayListOf<T>()

    for (x in xs) {
        if (x is T) {
            ts.add(x)
        }
    }

    return ts
}

fun box(): String {
    konst a1 = A(1)
    konst b2 = B(2)
    konst a3 = A(3)
    konst b4 = B(4)

    assertEquals(listOf(a1), test<A>(a1, b2), "test(a1, b2)")
    assertEquals(listOf(b2, b4), test<B>(a1, b2, a3, b4), "test<B>(a1, b2, a3, b4)")

    konst objects = arrayOf(a1, b2)
    assertEquals(listOf(b2, b4), test<B>(a1, a3, *objects, a3, b4), "test<B>(a1, a3, *objects, a3, b4)")

    return "OK"
}
