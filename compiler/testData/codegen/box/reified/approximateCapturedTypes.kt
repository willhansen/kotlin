// TARGET_BACKEND: JVM

// WITH_STDLIB
// Basically this test checks that no captured type used as argument for signature mapping

package test

class SwOperator<T>: Operator<List<T>, T>

interface Operator<R, T>

open class Inv<T>
class Obs<Y> {
    inline fun <reified X> lift(lift: Operator<out X, in Y>) = object : Inv<X>() {}
}

fun box(): String {
    konst o: Obs<CharSequence> = Obs()

    konst inv = o.lift(SwOperator())
    konst signature = inv.javaClass.genericSuperclass.toString()

    if (signature != "test.Inv<java.util.List<? extends java.lang.CharSequence>>") return "fail 1: $signature"

    return "OK"
}
