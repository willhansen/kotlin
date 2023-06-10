// WITH_STDLIB
// TARGET_BACKEND: JS_IR
// FILE: main.js
ArrayBuffer.isView = undefined;

// FILE: main.kt
fun box(): String {
    konst intArr = intArrayOf(5, 4, 3, 2, 1)
    konst result = IntArray(5).apply { intArr.copyInto(this) }

    assertEquals(result.joinToString(","), intArr.joinToString(","))
    assertEquals(js("ArrayBuffer.isView.called"), js("undefined"))

    return "OK"
}
