// EXPECTED_REACHABLE_NODES: 1288
package foo

class A(konst a: Int)

class Empty

konst x = 1
konst a = A(2)
konst e = Empty()

fun box(): String {
    if (x != 1) return "x != 1, it: $x"
    if (a.a != 2) return "a.a != 2, it: ${a.a}"

    return "OK"
}