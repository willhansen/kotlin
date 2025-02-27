// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: SERIALIZATION_REGRESSION
// IGNORE_BACKEND: JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_BACKEND: NATIVE
// IGNORE_BACKEND_K2: JVM_IR
// FIR status: konstidation failed. TODO decide if we want to fix KT-42020 for FIR as well
// MODULE: lib
// FILE: lib.kt

open class Base<T> {
    open fun foo(p1: T): String { return "p1:$p1" }
    open fun foo(p2: String): String { return "p2:$p2" }
}
class Derived : Base<String>()



// MODULE: main(lib)
// FILE: main.kt

fun box(): String {
    konst d = Derived()
    if (d.foo(p1 = "42") != "p1:42") return "FAIL1"
    if (d.foo(p2 = "24") != "p2:24") return "FAIL2"

    return "OK"
}