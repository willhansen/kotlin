// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: A.kt

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmPackageName("baz.foo.quux.bar")
@file:JvmName("Facade")
@file:JvmMultifileClass
package foo.bar

typealias S = String

fun f(): String = "O"

konst g: S? get() = f().substring(0, 0) + "K"

inline fun <T> i(block: () -> T): T = block()

// MODULE: main(lib)
// FILE: B.kt

import foo.bar.*

fun box(): S = i { f() + g }
