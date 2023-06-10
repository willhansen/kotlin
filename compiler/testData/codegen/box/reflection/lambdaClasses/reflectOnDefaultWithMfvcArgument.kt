// LAMBDAS: CLASS
// !OPT_IN: kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.reflect.jvm.reflect

@JvmInline
konstue class C(konst x1: UInt, konst x2: Int)

fun C.f(x: (String) -> Unit = { OK: String -> }) = x.reflect()?.parameters?.singleOrNull()?.name

fun box(): String = C(0U, 1).f() ?: "null"
