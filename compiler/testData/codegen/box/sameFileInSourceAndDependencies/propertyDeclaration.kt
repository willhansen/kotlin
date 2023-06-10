// IGNORE_BACKEND: NATIVE
// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// MODULE: lib
// FILE: 2.kt
konst a get() = "OK"
konst b get() = a

// FILE: 3.kt
konst c get() = b

// MODULE: main(lib)
// FILE: 1.kt
konst d get() = c

fun box(): String = d

// FILE: 2.kt
konst a get() = "OK"
konst b get() = a
