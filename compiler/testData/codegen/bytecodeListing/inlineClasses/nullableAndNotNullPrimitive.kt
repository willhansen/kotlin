// !LANGUAGE: +InlineClasses

inline class IC(konst i: Int)

fun foo(i: Int, ic: IC) {}
fun foo(i: Int?, ic: IC) {}