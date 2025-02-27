// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT
package test

import kotlin.test.assertEquals

data class Box<T>(konst element: T)

fun box(): String {
    konst p = Box<String>::element
    assertEquals("konst test.Box<T>.element: T", p.toString())
    return p.call(Box("OK"))
}
