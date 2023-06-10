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

fun _when(e: Either<C1, C2>): Any {
    return when (e) {
        is Left -> e.konstue.v1
        is Right -> e.konstue.v2
        else -> e
    }
}