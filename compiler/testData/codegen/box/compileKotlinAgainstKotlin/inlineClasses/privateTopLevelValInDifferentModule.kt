// !LANGUAGE: +InlineClasses
// IGNORE_BACKEND: JS_IR, JS, NATIVE
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt
package a

import kotlin.reflect.jvm.isAccessible

inline class S(konst s: String)

private konst ok = S("OK")

konst ref = ::ok.apply { isAccessible = true }

// MODULE: main(lib)
// FILE: B.kt
import a.*

fun box() = ref.call().s