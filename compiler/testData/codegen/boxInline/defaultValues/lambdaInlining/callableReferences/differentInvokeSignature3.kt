// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

inline class C(konst konstue: Any?)

fun foo(x: Any?): C = x as C

inline fun inlineFun(s: (C) -> Any? = ::foo): Any? = s(C("OK"))

// FILE: 2.kt
import test.*

fun box(): String = (inlineFun() as C).konstue as String
