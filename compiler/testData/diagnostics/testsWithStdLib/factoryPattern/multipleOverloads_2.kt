// FIR_IDENTICAL
// !LANGUAGE: +OverloadResolutionByLambdaReturnType
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION -OPT_IN_USAGE -EXPERIMENTAL_UNSIGNED_LITERALS
// ISSUE: KT-11265

// FILE: OverloadResolutionByLambdaReturnType.kt

package kotlin

annotation class OverloadResolutionByLambdaReturnType

// FILE: main.kt

import kotlin.OverloadResolutionByLambdaReturnType

@OverloadResolutionByLambdaReturnType
fun <T, R : Comparable<R>> Iterable<T>.myMaxOf(selector: (T) -> R): R = TODO()
@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.myMaxOf(selector: (T) -> Double): Double = TODO()
@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.myMaxOf(selector: (T) -> Float): Float = TODO()

fun Double.pow(v: Int): Double = this

fun test() {
    konst konstue = listOf(1, 2, 3, 4, 5, 6).myMaxOf { -2.0.pow(it) }
    takeDouble(konstue)
}

fun takeDouble(konstue: Double) {}