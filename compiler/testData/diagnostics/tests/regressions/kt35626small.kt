// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

class Result<T> {
    fun <R> map(transform: (T) -> R): Result<R> = TODO()
}

class TupleX<T1, T2, T3, T4>(
    konst _1: T1, konst _2: T2, konst _3: T3, konst _4: T4
)

fun <K1, K2, K3, K4> rules(res: Result<Any>):
        Result<TupleX<K1, K2, K3, K4>> {
    return res.map {
        @Suppress("UNCHECKED_CAST")
        TupleX(
            it as K1, it as K2, it as K3, it as K4
        )
    }
}

