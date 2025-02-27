// IGNORE_BACKEND: WASM
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): String {
        p.parameters
        p.returnType
        p.annotations
        return p.toString()
    }
}

konst prop: String by Delegate()

fun box() = if (prop == "konst prop: kotlin.String") "OK" else "Fail: $prop"
