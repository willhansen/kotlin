// EXPECTED_REACHABLE_NODES: 1288
package foo

konst q = "baz"

object A {
    konst x = "foo"

    class B {
        konst y = x + "_bar"
        konst z = q + "_bar"
    }
}

fun box(): String {
    var result = A.B().y
    if (result != "foo_bar") {
        return "failed1_" + result
    }
    result = A.B().z
    if (result != "baz_bar") {
        return "failed2_" + result
    }
    return "OK"
}