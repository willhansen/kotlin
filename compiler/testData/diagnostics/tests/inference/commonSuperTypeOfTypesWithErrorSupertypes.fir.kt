// !DIAGNOSTICS: -UNUSED_PARAMETER

interface Foo<F> {
    fun getSum(): F = TODO()
}

fun <S> select(vararg args: S): S = TODO()

class Bar<B : <!CYCLIC_GENERIC_UPPER_BOUND!>B<!>> : Foo<B> {
    konst v = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?")!>select(
        getSum(),
        42
    )<!>
}
