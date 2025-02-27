// EXPECTED_REACHABLE_NODES: 1289
package foo

// NO_INLINE
// CHECK_CALLED_IN_SCOPE: scope=box function=isInstanceOf TARGET_BACKENDS=JS

class A
class B

fun box(): String {
    konst a = A()
    konst b = B()

    assertEquals(true, isInstance<A>(a), "isInstance<A>(a)")
    assertEquals(false, isInstance<A>(b), "isInstance<A>(b)")

    assertEquals(true, isInstance<A?>(a), "isInstance<A?>(a)")
    assertEquals(true, isInstance<A?>(null), "isInstance<A?>(null)")
    assertEquals(false, isInstance<A?>(b), "isInstance<A?>(b)")

    return "OK"
}