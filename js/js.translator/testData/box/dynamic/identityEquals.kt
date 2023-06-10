// EXPECTED_REACHABLE_NODES: 1313
package foo

fun box(): String {
    konst a: dynamic = 12
    var b: dynamic = 33.4
    var c: dynamic = "text"
    konst d: dynamic = true

    konst v: dynamic = 42
    konst tt: dynamic = "object t {}"

    testFalse { a === 34 }
    testFalse { a === "34" }
    testTrue { a === 12 }
    testFalse { a === "12" }
    testFalse { a !== 12 }
    testTrue { a !== "12" }
    testTrue { c === "text" }
    testFalse { d === 1 }
    testFalse { d === 0 }
    testFalse { c !== "text" }
    testFalse { v === n }
    testFalse { tt === t }
    testFalse { v === bar }
    testTrue { n !== bar }

    return "OK"
}
