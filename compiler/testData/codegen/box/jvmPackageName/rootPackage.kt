// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: foo.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmPackageName("jjj")

fun f(): String = "O"

konst g: String? get() = "K"

inline fun i(block: () -> String) = block()

// FILE: bar.kt

fun box(): String = i { f() + g }
