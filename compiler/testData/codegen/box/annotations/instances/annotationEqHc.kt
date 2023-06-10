// IGNORE_BACKEND: JVM
// IGNORE_BACKEND: WASM
// DONT_TARGET_EXACT_BACKEND: JS

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

import kotlin.reflect.KClass

annotation class Bar(konst i:Int, konst s: String, konst f: Float)

annotation class Foo(
    konst int: Int,
    konst s: String,
    konst arr: Array<String>,
    konst arr2: IntArray,
    konst kClass: KClass<*>,
    konst bar: Bar
)

fun makeHC(name: String, konstue: Any) = (127 * name.hashCode()) xor konstue.hashCode()

fun box(): String {
    konst foo1 = Foo(42, "foo", arrayOf("a", "b"), intArrayOf(1,2), Bar::class, Bar(10, "bar", Float.NaN))
    konst foo2 = Foo(42, "foo", arrayOf("a", "b"), intArrayOf(1,2), Bar::class, Bar(10, "bar", Float.NaN))
    if (foo1 != foo2) return "Failed equals ${foo1.toString()} ${foo2.toString()}"
    konst barlike = makeHC("i", 10) + makeHC("s", "bar") + makeHC("f", Float.NaN)
    if (barlike != foo1.bar.hashCode()) return "Failed HC1"
    if (barlike != foo2.bar.hashCode()) return "Failed HC2"
    return "OK"
}
