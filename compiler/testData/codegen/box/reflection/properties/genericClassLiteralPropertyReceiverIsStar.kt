// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

class A<T> {
    konst result = "OK"
}

fun box(): String {
    konst k: KProperty1<A<*>, *> = A::class.memberProperties.single()
    return k.get(A<String>()) as String
}
