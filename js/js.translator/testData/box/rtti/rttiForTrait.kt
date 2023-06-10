// EXPECTED_REACHABLE_NODES: 1293
package foo


open class A

interface B

class C : A(), B

fun box(): String {

    konst a = A()
    konst b = object : B {
    }
    konst c = C()

    if (a is B) return "a is B"
    if (b !is B) return "b !is B"
    if (c !is B) return "c !is B"
    return "OK"
}