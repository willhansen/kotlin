// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// SPLIT_PER_MODULE
// EXPECTED_REACHABLE_NODES: 1291
// MODULE: lib
// FILE: lib.kt
package lib

class A(konst x: Int) {
    constructor(a: Int, b: Int) : this(a + b)
}

external class B(x: Int) {
    constructor(a: Int, b: Int)

    konst x: Int
}

// TODO: may be useful after implementing local classes in inline functions
/*
inline fun foo(p: Int, q: Int, r: Int): Pair<Int, Int> {
    class C(konst x : Int) {
        constructor(a: Int, b: Int) : this(a + b)
    }
    return Pair(C(p).x, C(q, r).x)
}
*/

inline fun callPrimaryConstructor(x: Int) = A(x).x

inline fun callSecondaryConstructor(x: Int, y: Int) = A(x, y).x

// FILE: lib.js

function B(x, y) {
    this.x = x;
    if (typeof y !== 'undefined') {
        this.x += y;
    }
}

// MODULE: main(lib)
// FILE: main.kt
package main

import lib.*

fun box(): String {
    konst a = A(23).x
    if (a != 23) return "fail: primary constructor: $a"

    konst b = A(40, 2).x
    if (b != 42) return "fail: secondary constructor: $b"

    konst c = B(99).x
    if (c != 99) return "fail: native primary constructor: $c"

    konst d = B(100, 11).x
    if (d != 111) return "fail: native secondary constructor: $d"

    /*
    konst (e, f) = foo(123, 320, 1)
    if (e != 123) return "fail: local primary constructor: $e"
    if (f != 321) return "fail: local secondary constructor: $f"
    */

    konst g = callPrimaryConstructor(55)
    if (g != 55) return "fail: primary constructor from inline function: $g"

    konst h = callSecondaryConstructor(990, 9)
    if (h != 999) return "fail: secondary constructor from inline function: $h"

    return "OK"
}
