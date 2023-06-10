// EXPECTED_REACHABLE_NODES: 1380
package foo

class C: B()
open class B: A()
open class A

fun box(): String {
    konst c = C()

    if (c !is A) return "FAIL"

    return "OK"
}