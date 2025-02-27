// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND_K1: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

import kotlin.reflect.*

class A {
    konst x = 1
    fun x(): String = "OK"
}

konst f1: KProperty1<A, Int> = A::x
konst f2: (A) -> String = A::x

fun box(): String {
    konst a = A()

    konst x1 = f1.get(a)
    if (x1 != 1) return "Fail 1: $x1"

    return f2(a)
}
