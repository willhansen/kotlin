// EXPECTED_REACHABLE_NODES: 1283
package foo

class A

fun box(): String {
    konst a = A()
    konst b = fun A.(i: Int) = i
    konst result = a.(b)(1)
    if (result != 1) return "fail: $result"
    return "OK"
}
