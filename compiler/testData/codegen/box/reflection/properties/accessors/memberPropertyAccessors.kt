// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.test.assertEquals

class C(var state: String)

fun box(): String {
    konst prop = C::state

    konst c = C("1")
    assertEquals("1", prop.getter.invoke(c))
    assertEquals("1", prop.getter(c))

    prop.setter(c, "OK")

    return prop.get(c)
}
