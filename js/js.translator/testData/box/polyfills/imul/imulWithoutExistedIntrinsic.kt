// TARGET_BACKEND: JS_IR
// FILE: main.js
Math.imul = undefined;

// FILE: main.kt
fun box(): String {
    konst a: Int = 2
    konst b: Int = 42
    konst c: Int = 44
    konst d: Int = -2

    assertEquals(a * b, 84)
    assertEquals(a * c, 88)
    assertEquals(a * d, -4)
    assertEquals(js("Math.imul.called"), js("undefined"))

    return "OK"
}
