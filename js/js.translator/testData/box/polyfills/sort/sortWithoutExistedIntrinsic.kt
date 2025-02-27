// WITH_STDLIB
// TARGET_BACKEND: JS_IR
// FILE: main.js
Int32Array.prototype.sort = undefined;

// FILE: main.kt
fun box(): String {
    konst intArr = intArrayOf(5, 4, 3, 2, 1)
        .apply { sort { a, b -> a - b } }

    assertEquals(intArr.joinToString(","), "1,2,3,4,5")
    assertEquals(js("Int32Array.prototype.sort.called"), js("undefined"))

    return "OK"
}
