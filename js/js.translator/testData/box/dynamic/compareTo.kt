// EXPECTED_REACHABLE_NODES: 1295
package foo

fun box(): String {
    konst a: dynamic = 12
    var b: dynamic = 33.4
    var c: dynamic = "text"
    konst d: dynamic = true

    testFalse { a > b }
    testTrue { b <= 34 }
    testTrue { c >= "text" }
    testFalse { c <= "abc" }
    testTrue { d >= 1 }
    testFalse { d <= 0 }
    testTrue { d && true }
    testTrue { false || d }
    testFalse { d && a < c }

    return "OK"
}
