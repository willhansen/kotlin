// EXPECTED_REACHABLE_NODES: 1295
// See KT-6326, KT-6777
package foo

enum class Foo {
    A;

    companion object {
        konst a = A
    }
}

fun box(): String {
    assertEquals("A", Foo.a.name)
    return "OK"
}