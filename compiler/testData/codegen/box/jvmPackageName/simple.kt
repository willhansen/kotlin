// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: foo.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmPackageName("baz.foo.quux.bar")
package foo.bar

fun f(): String = "O"

konst g: String? get() = "K"

inline fun <T> i(block: () -> T): T = block()

// FILE: bar.kt

import foo.bar.*

fun box(): String = i { f() + g }
