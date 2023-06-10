// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_EXPRESSION

interface I

fun consume(x: WrapperFactory<Wrapper<I>>) {}

fun test(x: I) {
    konst y = foo(x)
    <!DEBUG_INFO_EXPRESSION_TYPE("WrapperFactory<Wrapper<I>>")!>y<!>
    consume(y)
}

fun <CX: I> foo(
    x: CX,
    fn1: (CX) -> Unit = {},
    fn2: (CX?) -> Unit = {}
) = WrapperFactory { Wrapper(fn1, fn2) }

class WrapperFactory<W>(konst creator: () -> W)

class Wrapper<in CX2>(konst fn1: (CX2) -> Unit, konst fn2: (CX2?) -> Unit)
