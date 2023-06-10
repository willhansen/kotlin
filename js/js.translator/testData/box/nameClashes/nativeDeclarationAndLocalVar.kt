// EXPECTED_REACHABLE_NODES: 1282
package test

external fun foo(): dynamic

external fun bar(): dynamic

fun box(): String {
    konst foo = "local foo;"
    konst bar = "local bar;"
    konst result = foo + test.bar() + test.foo() + bar
    if (result != "local foo;global bar;global foo;local bar;") return "fail: $result"

    return "OK"
}