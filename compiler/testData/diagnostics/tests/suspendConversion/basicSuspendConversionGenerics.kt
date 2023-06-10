// !LANGUAGE: +SuspendConversion
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION

class Inv2<T, K>

fun <T> foo1(f: suspend (T) -> String): T = TODO()
fun <T> foo2(f: suspend () -> T): T = TODO()
fun <T, K> foo3(f: suspend (T) -> K): Inv2<T, K> = TODO()

fun <I> id(e: I): I = e

fun test(f: (Int) -> String, g: () -> String) {
    konst a0 = foo1(f)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>a0<!>

    konst a1 = foo2(g)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>a1<!>

    konst a2 = foo3(f)
    <!DEBUG_INFO_EXPRESSION_TYPE("Inv2<kotlin.Int, kotlin.String>")!>a2<!>

    konst a3 = foo1(id(f))
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>a3<!>

    konst a4 = foo2(id(g))
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>a4<!>

    konst a5 = foo3(id(f))
    <!DEBUG_INFO_EXPRESSION_TYPE("Inv2<kotlin.Int, kotlin.String>")!>a5<!>
}
