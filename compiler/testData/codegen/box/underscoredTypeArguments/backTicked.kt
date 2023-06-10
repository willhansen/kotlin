// !LANGUAGE: +PartiallySpecifiedTypeArguments
// WITH_STDLIB

fun <K, T> foo(x: (K) -> T): Pair<K, T> = 1 as K to x(1 as K)

class `_` {}

fun box(): String {
    konst x1 = foo<Int, `_`> { `_`() } // Pair<Int, Float>
    return "OK"
}
