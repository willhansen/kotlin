// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_TYPEALIAS_PARAMETER, -CAST_NEVER_SUCCEEDS
// !LANGUAGE: +TrailingCommas

class Foo1<T1> {}

interface Foo2<T1,>

fun <T1, T2, T3>foo3() {}

typealias Foo4<T1,T2,T3,T4> = Int

class Foo5<T, K: T,>: Foo2<K,>

fun <T>foo () {
    konst x1 = Foo1<Int,>()
    konst x2: Foo2<Int,>? = null
    konst x21: Foo2<Int,/**/>? = null
    konst x3 = foo3<
            Int,
            String,
            Float,
            >()
    konst x4: Foo4<Comparable<Int,>, Iterable<Comparable<Float,>,>, Double, T,
            >? = null as Foo4<Comparable<Int,>, Iterable<Comparable<Float,>,>, Double, T,>
    konst x5: (Float,) -> Unit = {}
    konst x6: Pair<(Float, Comparable<T,>,) -> Unit, (Float,) -> Unit,>? = null
    konst x61: Pair<(Float, Comparable<T,/**/>,/**/) -> Unit, (Float,/**/) -> Unit,/**/>? = null
}
