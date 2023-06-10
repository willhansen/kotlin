// FIR_IDENTICAL
// SKIP_KLIB_TEST
// IGNORE_BACKEND_K1: JS_IR
//   Ignore reason: there is a js name clash between function `a()` and property `a`
package test

class A() {
    constructor(x: Int) : this()
    fun b() {}
    fun a() {}
    konst b: Int = 1
    konst a: Int = 2
    constructor(x: String) : this()
    konst Int.b: String get() = "b"
    fun String.b() {}
    konst Int.a: String get() = "a"
    fun String.a() {}
    constructor(x: Double) : this()
}
