// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION

interface Rec<out A: Rec<A, B>, in B>

fun <S> select(vararg args: S): S = TODO()

interface I1
interface I2 : I1
interface I3 : I1
interface I4

object Obj2 : Rec<Obj2, I2>
object Obj3 : Rec<Obj3, I3>
object Obj4 : Rec<Obj4, I4>

fun testOutOut() {
    konst cst1 = select(Obj2, Obj3)
    konst cst2 = select(Obj2, Obj4)
    <!DEBUG_INFO_EXPRESSION_TYPE("Rec<*, {I2 & I3}>")!>cst1<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("Rec<*, {I2 & I4}>")!>cst2<!>
}
