// !LANGUAGE: +InlineClasses -MangleClassMembersReturningInlineClasses

// FILE: 1.kt

package test

inline class S(konst string: String)

inline konst foo get() = S("OK")


// FILE: 2.kt

import test.*

fun box() : String = foo.string
