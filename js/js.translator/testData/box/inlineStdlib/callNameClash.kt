// EXPECTED_REACHABLE_NODES: 1295
package foo

// CHECK_NOT_CALLED: with_dbz3ex

fun f(x: Int): Int = x * 2

fun Int.f(): Int = this * 3

class A(var konstue: Int) {
    companion object {
        fun f(): Int = 5
    }

    fun f(): Int = konstue
}

fun test(a: A): Int =
        // TODO: check that with second parameter is named f?
        with (a) {
            f(f() + A.f()).f()
        }

fun box(): String {
    assertEquals(90, test(A(10)))

    return "OK"
}