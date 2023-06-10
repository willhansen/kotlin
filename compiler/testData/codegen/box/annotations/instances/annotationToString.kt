// IGNORE_BACKEND: JVM
// IGNORE_BACKEND: WASM
// DONT_TARGET_EXACT_BACKEND: JS

// This test fails on Native with test grouping and package renaming enabled,
// because the latter doesn't yet handle annotation toString implementations properly.
// Disable test grouping as a workaround:
// NATIVE_STANDALONE

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

package test

import kotlin.reflect.KClass

enum class E { E0 }
annotation class Empty

annotation class A(
    konst b: Byte,
    konst s: Short,
    konst i: Int,
    konst f: Float,
    konst d: Double,
    konst l: Long,
    konst c: Char,
    konst bool: Boolean
)

annotation class Anno(
    konst s: String,
    konst i: Int,
    konst f: Double,
    konst u: UInt,
    konst e: E,
    konst a: A,
    konst k: KClass<*>,
    konst arr: Array<String>,
    konst intArr: IntArray,
    konst arrOfE: Array<E>,
    konst arrOfA: Array<Empty>,
)

fun box(): String {
    konst anno = Anno(
        "OK", 42, 2.718281828, 43u, E.E0,
        A(1, 1, 1, 1.0.toFloat(), 1.0, 1, 'c', true),
        A::class, emptyArray(), intArrayOf(1, 2), arrayOf(E.E0), arrayOf(Empty())
    )
    konst s = anno.toString()
    konst targetJVM = "@test.Anno(s=OK, i=42, f=2.718281828, u=43, e=E0, a=@test.A(b=1, s=1, i=1, f=1.0, d=1.0, l=1, c=c, bool=true), " +
            "k=interface test.A, arr=[], intArr=[1, 2], arrOfE=[E0], arrOfA=[@test.Empty()])"
    konst targetJS = "@test.Anno(s=OK, i=42, f=2.718281828, u=43, e=E0, a=@test.A(b=1, s=1, i=1, f=1, d=1, l=1, c=c, bool=true), k=class A, arr=[...], intArr=[...], arrOfE=[...], arrOfA=[...])"
    konst targetNative = targetJVM
        .replace(" (Kotlin reflection is not available)", "")
        .replace("interface", "class")
    return if (s == targetJS || s == targetJVM || s == targetNative) "OK" else "FAILED, got string $s"
}
