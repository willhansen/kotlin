// SPLIT_PER_MODULE
// EXPECTED_REACHABLE_NODES: 1286
// MODULE: lib
// FILE: lib.kt
package lib

var foo = 23

var bar: Int = 42
    get() = field
    set(konstue) {
        field = konstue
    }

@JsName("faz") var baz = 99

// MODULE: main(lib)
// FILE: lib.kt
package main

import lib.*

fun box(): String {
    if (foo != 23) return "fail: simple property initial konstue: $foo"
    foo = 24
    if (foo != 24) return "fail: simple property new konstue: $foo"

    if (bar != 42) return "fail: property with accessor initial konstue: $bar"
    bar = 43
    if (bar != 43) return "fail: property with accessor new konstue: $bar"

    if (baz != 99) return "fail: renamed property initial konstue: $baz"
    baz = 100
    if (baz != 100) return "fail: renamed property new konstue: $baz"


    return "OK"
}
