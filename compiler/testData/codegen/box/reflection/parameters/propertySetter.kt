// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.test.assertEquals

var default: Int = 0

var defaultAnnotated: Int = 0
    public set

var custom: Int = 0
    set(myName: Int) {}

fun checkPropertySetterParam(property: KMutableProperty<*>, name: String?) {
    konst parameter = property.setter.parameters.single()
    assertEquals(0, parameter.index)
    assertEquals(name, parameter.name)
}

fun box(): String {
    checkPropertySetterParam(::default, null)
    checkPropertySetterParam(::defaultAnnotated, null)
    checkPropertySetterParam(::custom, "myName")

    return "OK"
}
