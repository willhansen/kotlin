// TARGET_BACKEND: JS_IR
// FIR_IDENTICAL

konst d: dynamic = 1

konst p: Int = d

fun test1(d: dynamic): Int = d

fun test2(d: dynamic): Any = d

fun test3(d: dynamic): Any? = d

fun test4(d: dynamic): String = d.member(1, 2, 3)
