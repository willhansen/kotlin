// EXPECTED_REACHABLE_NODES: 1296
package foo

class D

open class A

open class B : A()

open class C : B()

fun box(): String {
    konst a: Any = A()
    konst b: Any = B()
    konst c: Any = C()
    if (a !is A) return "a !is A"

    konst t = a is A
    if (!t) return "t = a is A; t != true"

    if (b !is A) return "b !is A"
    if (b !is B) return "b !is B"

    if (c !is A) return "c !is A"
    if (c !is B) return "c !is B"
    if (c !is C) return "c !is C"

    if (a is D) return "a is D"
    if (b is D) return "b is D"

    return "OK"
}