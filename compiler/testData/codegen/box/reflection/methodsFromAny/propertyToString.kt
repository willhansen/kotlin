// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

package test

import kotlin.test.assertEquals

konst top = 42
var top2 = -23

konst String.ext: Int get() = 0
var IntRange?.ext2: Int get() = 0; set(konstue) {}

class A(konst mem: String)
class B(var mem: String)

fun assertToString(s: String, x: Any) {
    assertEquals(s, x.toString())
}

fun box(): String {
    assertToString("konst top: kotlin.Int", ::top)
    assertToString("var top2: kotlin.Int", ::top2)
    assertToString("konst kotlin.String.ext: kotlin.Int", String::ext)
    assertToString("var kotlin.ranges.IntRange?.ext2: kotlin.Int", IntRange::ext2)
    assertToString("konst test.A.mem: kotlin.String", A::mem)
    assertToString("var test.B.mem: kotlin.String", B::mem)
    assertToString("getter of konst top: kotlin.Int", ::top.getter)
    assertToString("getter of var top2: kotlin.Int", ::top2.getter)
    assertToString("setter of var top2: kotlin.Int", ::top2.setter)
    assertToString("getter of konst test.A.mem: kotlin.String", A::mem.getter)
    assertToString("getter of var test.B.mem: kotlin.String", B::mem.getter)
    assertToString("setter of var test.B.mem: kotlin.String", B::mem.setter)
    return "OK"
}
