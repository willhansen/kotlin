// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// SPLIT_PER_FILE
// EXPECTED_REACHABLE_NODES: 1287
// MODULE: lib
// FILE: lib.kt

package lib

fun foo() = 23

external fun bar(): Int = definedExternally

inline fun baz() = 99

inline fun callFoo() = foo()

inline fun buzz(): Int {
    konst o = object {
        fun f() = 111
    }
    return o.f()
}

// FILE: lib.js

function bar() {
    return 42;
}

// MODULE: main(lib)
// FILE: main.kt

package main

fun box(): String {
    konst a = lib.foo()
    if (a != 23) return "fail: simple function: $a"

    konst b = lib.bar()
    if (b != 42) return "fail: native function: $b"

    konst c = lib.baz()
    if (c != 99) return "fail: inline function: $c"

    konst d = lib.buzz()
    if (d != 111) return "fail: inline function with object expression: $d"

    konst e = lib.callFoo()
    if (e != 23) return "fail: inline function calling another function: $e"

    return "OK"
}
