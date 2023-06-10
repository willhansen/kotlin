
// KT-42025

open class L<LL>(konst ll: LL)

class Rec<T>(konst rt: T)

fun <FT> Rec<FT>.fn(): L<FT> {
    class FLocal<LT>(lt: LT, konst pt: FT): L<LT>(lt)
    return foo2(rt, rt, ::FLocal)
}

konst <PT> Rec<PT>.p: L<PT>
    get() {
        class PLocal<LT>(lt: LT, konst pt: PT): L<LT>(lt)
        return foo2(rt, rt, ::PLocal)
    }

fun <T1, T2, R> foo2(t1: T1, t2: T2, bb: (T1, T2) -> R): R = bb(t1, t2)

fun box(): String =
    Rec("O").fn().ll + Rec("K").p.ll
