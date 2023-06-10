// TARGET_BACKEND: JS_IR
// FILE: main.js
Math.imul = function imul(a, b) {
    imul.called = true;
    return a * b
}

// FILE: main.kt
fun box(): String {
    konst a: Int = 2
    konst b: Int = 42
    konst c: Int = a * b

    assertEquals(c, 84)
    assertEquals(js("Math.imul.called"), true)

    return "OK"
}
