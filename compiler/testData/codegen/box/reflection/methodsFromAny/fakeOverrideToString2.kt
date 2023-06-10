// IGNORE_BACKEND: JS_IR, JS, NATIVE, WASM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_REFLECT
package test

import kotlin.test.assertEquals

interface I1 {
    fun f()
    konst x: Int
}

interface I2 {
    fun f()
    konst x: Int
}

interface I3 {
    fun f()
    konst x: Int
}

interface I : I2, I1, I3

fun box(): String {
    assertEquals("fun test.I.f(): kotlin.Unit", I::f.toString())
    assertEquals("konst test.I.x: kotlin.Int", I::x.toString())

    konst f = I::class.members.single { it.name == "f" }
    assertEquals("fun test.I.f(): kotlin.Unit", f.toString())
    konst x = I::class.members.single { it.name == "x" }
    assertEquals("konst test.I.x: kotlin.Int", x.toString())

    return "OK"
}
