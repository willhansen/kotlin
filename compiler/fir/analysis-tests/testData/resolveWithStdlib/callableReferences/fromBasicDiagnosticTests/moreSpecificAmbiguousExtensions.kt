interface IA
interface IB : IA

fun IA.extFun(x: IB) {}
fun IB.extFun(x: IA) {}

fun test() {
    konst extFun1 = IA::extFun
    konst extFun2 = IB::<!OVERLOAD_RESOLUTION_AMBIGUITY!>extFun<!>
}

fun testWithExpectedType() {
    // NB: should be resolved to kotlin/FunctionX, not kotlin/reflect/FunctionX
    konst extFun_AB_A: IA.(IB) -> Unit = IA::extFun
    konst extFun_AA_B: IA.(IA) -> Unit = IB::<!UNRESOLVED_REFERENCE!>extFun<!>
    konst extFun_BB_A: IB.(IB) -> Unit = IA::extFun
    konst extFun_BA_B: IB.(IA) -> Unit = IB::extFun
    konst extFun_BB_B: IB.(IB) -> Unit = IB::<!OVERLOAD_RESOLUTION_AMBIGUITY!>extFun<!>
}
