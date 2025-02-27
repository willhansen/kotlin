// EXPECTED_REACHABLE_NODES: 1286
package foo

// CHECK_NOT_CALLED: castTo

class A
class B

inline
fun <reified T> Any?.castTo(): T? = this as? T?

fun box(): String {
    konst a: Any? = A()
    konst nil: Any? = null
    konst b: Any? = B()

    assertEquals(a, a.castTo<A>(), "a")
    assertEquals(null, nil.castTo<A>(), "nil")
    assertEquals(null, b.castTo<A>(), "b")

    return "OK"
}
