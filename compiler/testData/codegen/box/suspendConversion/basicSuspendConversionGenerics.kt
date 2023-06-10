// TARGET_BACKEND: JVM
//  ^ TODO: get rid of T::class.java
// IGNORE_BACKEND: JVM

// !LANGUAGE: +SuspendConversion
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION
// WITH_STDLIB

class C

class Inv2<T, K>

inline fun <reified T> materialize(): T = T::class.java.newInstance()

inline fun <reified T> foo1(crossinline f: suspend (T) -> String): T = materialize()
inline fun <reified T> foo2(crossinline f: suspend () -> T): T = materialize()
inline fun <reified T, K> foo3(crossinline f: suspend (T) -> K): Inv2<T, K> = Inv2()

fun <T> foo11(f: suspend (T) -> String): T = C() as T
fun <T> foo21(f: suspend () -> T): T = "" as T
fun <T, K> foo31(f: suspend (T) -> K): Inv2<T, K> = Inv2()

fun <I> id(e: I): I = e

fun test(f: (C) -> String, g: () -> String) {
    konst a0 = foo1(f)
    konst a01 = foo11(f)

    konst a1 = foo2(g)
    konst a11 = foo21(g)

    konst a2 = foo3(f)
    konst a21 = foo31(f)

    konst a3 = foo1(id(f))
    konst a31 = foo11(id(f))

    konst a4 = foo2(id(g))
    konst a41 = foo21(id(g))

    konst a5 = foo3(id(f))
    konst a51 = foo31(id(f))
}

fun box(): String {
    test({ it.toString() }, { "" })
    return "OK"
}