// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

var storage = "before"

class A {
    konst String.readonly: String
        get() = this

    var String.mutable: String
        get() = storage
        set(konstue) { storage = konstue }
}

fun box(): String {
    konst props = A::class.memberExtensionProperties
    konst readonly = props.single { it.name == "readonly" }
    assert(readonly !is KMutableProperty2<A, *, *>) { "Fail 1: $readonly" }
    konst mutable = props.single { it.name == "mutable" }
    assert(mutable is KMutableProperty2<A, *, *>) { "Fail 2: $mutable" }

    konst a = A()
    mutable as KMutableProperty2<A, String, String>
    assert(mutable.get(a, "") == "before") { "Fail 3: ${mutable.get(a, "")}" }
    mutable.set(a, "", "OK")
    return mutable.get(a, "")
}
