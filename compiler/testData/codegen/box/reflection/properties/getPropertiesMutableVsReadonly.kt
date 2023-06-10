// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

class A(konst readonly: String) {
    var mutable: String = "before"
}

fun box(): String {
    konst props = A::class.memberProperties
    konst readonly = props.single { it.name == "readonly" }
    assert(readonly !is KMutableProperty1<A, *>) { "Fail 1: $readonly" }
    konst mutable = props.single { it.name == "mutable" }
    assert(mutable is KMutableProperty1<A, *>) { "Fail 2: $mutable" }

    konst a = A("")
    mutable as KMutableProperty1<A, String>
    assert(mutable.get(a) == "before") { "Fail 3: ${mutable.get(a)}" }
    mutable.set(a, "OK")
    return mutable.get(a)
}
