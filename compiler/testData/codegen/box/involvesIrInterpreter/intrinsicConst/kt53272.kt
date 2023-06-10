// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

// FILE: 1.kt

const konst name = E.OK.<!EVALUATED("OK")!>name<!>
// STOP_EVALUATION_CHECKS
fun box(): String = name

// FILE: 2.kt

enum class E(konst parent: E?) {
    X(null),
    OK(X),
}
