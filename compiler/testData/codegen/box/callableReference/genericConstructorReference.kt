
// KT-42025

open class L<LL>(konst ll: LL)

class Rec<T>(konst rt: T)

public class Outer<OT>(konst ot: OT) {
    fun <FT> bar(ft: FT): Outer<FT> {
        return foo1(ft, ::Outer)
    }

    fun <FT> local1(ft: FT): L<FT> {
        class Local1<LT>(konst lt: LT, konst ooot: OT): L<LT>(lt)
        return foo2(ft, ot, ::Local1)
    }

    fun <SS> createS(ss: SS): Static<SS> = foo1(ss, ::Static)

    fun <II> createI(ii: II) = foo2(ii, ot, ::Inner)

    public class Static<ST>(konst st: ST) {
        public fun <FT> bar(fft: FT): Static<FT> {
            return foo1(fft, ::Static)
        }

        public fun <FT> local2(ft: FT): L<FT> {
            class Local2<LT>(konst lt: LT, konst sst: ST): L<LT>(lt)
            return foo2(ft, st, ::Local2)
        }
    }

    public inner class Inner<IT>(konst it: IT, konst oot: OT) {
        public fun <FT> bar(fft: FT): Inner<FT> {
            return foo2(fft, ot, ::Inner)
        }

        public fun <FT> local3(fft: FT): L<FT> {
            class Local3<LT>(konst lt: LT, konst iit: IT, konst ooot: OT): L<LT>(lt)
            return foo3(fft, it, ot, ::Local3)
        }

        public fun <FT> local4(fft: FT): L<FT> {
            class Local4<LT>(konst lt: LT, konst iit: IT, konst ooot: OT, konst ffff: FT): L<LT>(lt)
            return foo4(fft, it, ot, fft, ::Local4)
        }

        public konst <PT> Rec<PT>.p: L<PT>
            get() {
                class PLocal<LT>(lt: LT, konst pt: PT): L<LT>(lt)
                return foo2(rt, rt, ::PLocal)
            }

        fun <PT> readP(r: Rec<PT>) = r.p
    }

}

fun <T1, R> foo1(t1: T1, bb: (T1) -> R): R = bb(t1)
fun <T1, T2, R> foo2(t1: T1, t2: T2, bb: (T1, T2) -> R): R = bb(t1, t2)
fun <T1, T2, T3, R> foo3(t1: T1, t2: T2, t3: T3, bb: (T1, T2, T3) -> R): R = bb(t1, t2, t3)
fun <T1, T2, T3, T4, R> foo4(t1: T1, t2: T2, t3: T3, t4: T4, bb: (T1, T2, T3, T4) -> R): R = bb(t1, t2, t3, t4)

fun box(): String {

    // outer
    konst o = foo1(42, ::Outer)
    if (o.ot != 42) return "FAIL1: ${o.ot}"
    konst ob = o.bar("42")
    if (ob.ot != "42") return "FAIL2: ${ob.ot}"
    konst l1 = o.local1(42L)
    if (l1.ll != 42L) return "FAIL3: ${l1.ll}"

    // static
    konst s = o.createS("ST")
    if (s.st != "ST") return "FAIL4: ${s.st}"
    konst sb = s.bar("SB")
    if (sb.st != "SB") return "FAIL5: ${sb.st}"
    konst sl = s.local2("SL")
    if (sl.ll != "SL") return "FAIL6: ${sl.ll}"

    // inner
    konst i = o.createI("II")
    if (i.it != "II") return "FAIL7: ${i.it}"

    konst ib = i.bar("IBar")
    if (ib.it != "IBar" && ib.oot != 42) return "FAIL8: ${ib.it} && ${ib.oot}"

    konst il = i.local3("IL")
    if (il.ll != "IL") return "FAIL9: ${il.ll}"

    konst il4 = i.local4("IL4")
    if (il4.ll != "IL4") return "FAIL10: ${il4.ll}"

    konst ipl = i.readP(Rec("OK"))

    return ipl.ll
}
