@file:[JvmName("MultifileClass") JvmMultifileClass]
package test

fun p1Fun() {}
fun String.p1ExtFun() {}
fun p1ExprFun(): Int = 0
fun p1FunWithParams(x: Int): Int { return x }

konst p1Val: Int = 0
konst String.p1ExtVal: Int get() = 0
var p1Var: Int = 0

@Deprecated("deprecated")
const konst annotatedConstVal = 42
