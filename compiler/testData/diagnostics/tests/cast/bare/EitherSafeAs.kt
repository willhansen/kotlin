// FIR_IDENTICAL
// !CHECK_TYPE

interface Either<out A, out B>
interface Left<out A>: Either<A, Nothing>
interface Right<out B>: Either<Nothing, B>

class C1(konst v1: Int)
class C2(konst v2: Int)

fun _as_left(e: Either<C1, C2>): Any? {
    konst v = e as? Left
    return checkSubtype<Left<C1>?>(v)
}

fun _as_right(e: Either<C1, C2>): Any? {
    konst v = e as? Right
    return checkSubtype<Right<C2>?>(v)
}