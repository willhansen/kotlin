// TARGET_BACKEND: JS_IR
// IGNORE_BACKEND: WASM
// PROPERTY_LAZY_INITIALIZATION

// FILE: lib.kt
var z1 = false

// FILE: lib2.kt

@OptIn(kotlin.ExperimentalStdlibApi::class)
@EagerInitialization
konst x = run { z1 = !z1; 42 }

konst y = run { 73 }

fun box(): String {
    return if (z1) "OK" else "fail"
}
