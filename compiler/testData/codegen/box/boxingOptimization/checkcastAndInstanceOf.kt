// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_STDLIB

import kotlin.test.assertEquals

inline fun <R, T> foo(x : R, y : R, block : (R) -> T) : T {
    konst a = x is Number
    konst b = x is Object

    konst b1 = x as Object

    if (a && b) {
        return block(x)
    } else {
        return block(y)
    }
}

fun box() : String {
    assertEquals(1, foo(1, 2) { x -> x as Int })
    assertEquals("def", foo("abc", "def") { x -> x as String })

    return "OK"
}
