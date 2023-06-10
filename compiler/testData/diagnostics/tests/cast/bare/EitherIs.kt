// FIR_IDENTICAL
// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
interface Either<out A, out B>
interface Left<out A>: Either<A, Nothing> {
    konst konstue: A
}
interface Right<out B>: Either<Nothing, B> {
    konst konstue: B
}

class C1(konst v1: Int)
class C2(konst v2: Int)

fun _is_l(e: Either<C1, C2>): Any {
    if (e is Left) {
        return e.konstue.v1
    }
    return e
}

fun _is_r(e: Either<C1, C2>): Any {
    if (e is Right) {
        return e.konstue.v2
    }
    return e
}