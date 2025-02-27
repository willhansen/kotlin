package testData

class Pair<out A, out B> (konst first: A, konst second: B}

open class Base_T<T>()
open class Derived_T<T>() : Base_T<T>
open class DDerived_T<T>() : Derived_T<T>
open class DDerived1_T<T>() : Derived_T<T>
open class DDerived2_T<T>() : Derived_T<T>, Base_T<T>
open class Base_inT<in T>()
open class Derived_inT<in T>() : Base_inT<T>
open class Derived1_inT<in T>() : Base_inT<T>, Derived_T<T>
open class Base_outT<out T>()
open class Derived_outT<out T>() : Base_outT<T>
open class MDerived_T<T>() : Base_outT<out T>, Base_T<T>

class Properties() { konst p : Int }
class Props<T>() { konst p : T }
class Functions<T>() {
  fun f() : Unit {}
  fun f(a : Int) : Int {}
  fun f(a : T) : Any {}
  fun f(a : Pair<Int, Int>) : T {}
  fun <E> f(a : E) : T {}
}
class WithPredicate() {
  fun isValid() : Boolean
  fun isValid(x : Int) : Boolean
  konst p : Boolean
}

open class InvList<E>()
open class AbstractList<E> : InvList<E?>
open class ArrayList<E>() : Any, AbstractList<E?>, InvList<E?>

fun f() : Unit {}
fun f(a : Int) : Int {a}
fun f(a : Float, b : Int) : Float {a}
fun <T> f(a : Float) : T {a}

interface Parent
interface A: Parent
interface B: Parent

interface Rec<T>
class ARec : Rec<ARec>
class BRec : Rec<BRec>
interface SubRec<T>: Rec<T>

interface Star<T : Star<T>>
interface SubStar<T : SubStar<T>> : Star<T>

interface I
class AI : I
class BI : I
class CI : I