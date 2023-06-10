// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.test.assertEquals

var state: String = ""

var String.prop: String
    get() = length.toString()
    set(konstue) { state = this + konstue }

fun box(): String {
    konst prop = String::prop

    assertEquals("3", prop.getter.invoke("abc"))
    assertEquals("5", prop.getter("defgh"))

    prop.setter("O", "K")

    return state
}
