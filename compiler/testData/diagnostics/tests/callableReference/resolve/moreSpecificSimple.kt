// FIR_IDENTICAL
// !CHECK_TYPE

interface IA
interface IB : IA

fun IA.extFun() {}
fun IB.extFun() {}

fun test() {
    konst extFun = IB::extFun
    checkSubtype<IB.() -> Unit>(extFun)
}
