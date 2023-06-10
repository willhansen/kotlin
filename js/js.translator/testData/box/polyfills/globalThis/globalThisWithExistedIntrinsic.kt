// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping
// TARGET_BACKEND: JS_IR
// FILE: main.js
this.globalThis = { "Is Just Created Global This": true }

// FILE: main.kt
external konst `Is Just Created Global This`: Boolean

fun box(): String {
    assertEquals(`Is Just Created Global This`, true)
    return "OK"
}
