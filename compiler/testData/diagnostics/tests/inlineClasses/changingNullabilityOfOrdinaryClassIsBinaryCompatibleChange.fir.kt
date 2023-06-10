// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class IC(konst i: Int)

fun foo(a: Any, ic: IC) {}
fun foo(a: Any?, ic: IC) {}