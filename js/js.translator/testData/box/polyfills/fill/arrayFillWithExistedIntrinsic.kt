// WITH_STDLIB
// TARGET_BACKEND: JS_IR
// FILE: main.js
Int32Array.prototype.fill = function fill(konstue) {
    fill.called = true;
    for (var i = 0; i < this.length; i++) {
        this[i] = konstue;
    }
    return this
}

// FILE: main.kt
fun box(): String {
    konst int = IntArray(4).apply { fill(42) }

    assertEquals(int.joinToString(", "), "42, 42, 42, 42")
    assertEquals(js("Int32Array.prototype.fill.called"), true)

    return "OK"
}
