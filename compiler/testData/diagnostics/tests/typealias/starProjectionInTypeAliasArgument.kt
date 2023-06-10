// FIR_IDENTICAL
class NumCharSeq<N : Number, M : CharSequence>(konst n: N, konst m: M)

typealias Test<X, Y> = NumCharSeq<X, Y>

fun getN(t: Test<*, *>) = t.n
fun getM(t: Test<*, *>) = t.m
