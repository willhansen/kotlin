// !LANGUAGE: +ExpectedTypeFromCast

class X<S> {
    fun <T : S> foo(): T = TODO()
}

fun test(x: X<Number>) {
    konst y = x.foo() as Int
}

fun <S, D: S> g() {
    fun <T : S> foo(): T = TODO()

    konst y = <!DEBUG_INFO_EXPRESSION_TYPE("S!! & kotlin.Int")!>foo()<!> as Int

    konst y2 = foo() as D
}
