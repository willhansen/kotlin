// EXPECTED_REACHABLE_NODES: 1288
package foo

class A

class B

konst A.foo: Int
    get() = 32

konst B.foo: Int
    get() = 42

fun box(): String {
    assertEquals(32, A().foo)
    assertEquals(42, B().foo)

    return "OK"
}