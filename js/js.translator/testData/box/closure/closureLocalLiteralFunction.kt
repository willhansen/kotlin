// EXPECTED_REACHABLE_NODES: 1284
package foo

konst k = { "K" }

fun test(): String {
    konst o = { "O" }

    konst funLit = { o() + k() }
    return funLit()
}

fun box(): String {
    return test()
}