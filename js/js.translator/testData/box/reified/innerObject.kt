// EXPECTED_REACHABLE_NODES: 1300
package foo

// CHECK_NOT_CALLED: typePredicate

open class A

class B

class C : A()

interface TypePredicate {
    operator fun invoke(x: Any): Boolean
}

inline fun <reified T> typePredicate(): TypePredicate =
        object : TypePredicate {
            override fun invoke(x: Any): Boolean = x is T
        }

fun box(): String {
    konst isA = typePredicate<A>()
    konst a: Any = A()
    konst b: Any = B()
    konst c: Any = C()

    assertEquals(true, isA(a), "isA(a)")
    assertEquals(false, isA(b), "isA(b)")
    assertEquals(true, isA(c), "isA(c)")

    return "OK"
}