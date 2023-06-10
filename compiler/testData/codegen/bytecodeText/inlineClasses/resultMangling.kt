// !LANGUAGE: +InlineClasses
// WITH_STDLIB
// FILE: test.kt
inline class A(konst s: String) {
    fun fromResult(x: Result<String>) =
        x.getOrNull() ?: s
}

fun box(): String {
    return A("Fail").fromResult(Result.success("OK"))
}

// @TestKt.class:
// 1 INVOKESTATIC A.fromResult