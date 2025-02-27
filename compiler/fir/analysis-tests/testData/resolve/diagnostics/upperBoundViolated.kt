// COMPARE_WITH_LIGHT_TREE
interface A

class B<T> where T : A
class C : A
typealias GGG = C
typealias HHH = GGG
typealias JJJ = B<C>

fun <T : A> fest() {

}

fun test() {
    konst b1 = B<<!UPPER_BOUND_VIOLATED!>Int<!>>()
    konst b2 = B<C>()
    konst b3 = B<<!UPPER_BOUND_VIOLATED!>Any?<!>>()
    konst b4 = B<<!UNRESOLVED_REFERENCE!>UnexistingType<!>>()<!UNRESOLVED_REFERENCE!>NL<!><!SYNTAX!><<!>Int<!SYNTAX{LT}!><!SYNTAX!>><!>()<!>NumberPhile<!SYNTAX!><!>
    konst b5 = B<<!UPPER_BOUND_VIOLATED!>B<<!UNRESOLVED_REFERENCE!>UnexistingType<!>><!>>()
    fest<<!UPPER_BOUND_VIOLATED!>Boolean<!>>()
    fest<C>()
    fest<HHH>()
    fest<<!UPPER_BOUND_VIOLATED!>JJJ<!>>()
}

open class S<F, G : F>
class T<U, Y : U> : S<U, Y>()
class P<T0: Number, T1>
class P1<T2 : Number, T3 : Number>


fun <K, L : K> rest() {
    konst o1 = S<K, L>()
    konst o2 = S<K, K>()
    konst o3 = S<L, L>()

    konst o4 = S<S<K, L>, T<K, L>>()
    konst o5 = S<S<K, L>, <!UPPER_BOUND_VIOLATED!>T<K, K><!>>()
    konst o6 = S<S<L, L>, <!UPPER_BOUND_VIOLATED!>T<K, L><!>>()

    konst o7 = S<Any, T<S<K, L>, <!UPPER_BOUND_VIOLATED!>String<!>>>()
    konst o8 = S<Any, T<S<K, L>, Nothing>>()
    konst o9 = P<<!UPPER_BOUND_VIOLATED!>String<!>, P1<<!UPPER_BOUND_VIOLATED!>String<!>, <!UPPER_BOUND_VIOLATED!>String<!>>>()
}

class NumColl<T : Collection<Number>>
typealias NL<K> = NumColl<List<K>>
konst test7 = NL<Int>()<!UNRESOLVED_REFERENCE!>NumberPhile<!><!SYNTAX!><!>
konst test8 = <!UPPER_BOUND_VIOLATED!>NL<String>()<!>

class NumberPhile<T: Number>(x: T)
konst np1 = NumberPhile(10)
konst np2 = NumberPhile(<!ARGUMENT_TYPE_MISMATCH!>"Test"<!>)

class Test1<S1 : Test1<S1, K>, K : Any>
class Test2<S2 : Test1<S2, *>>

class Test3<S3 : Test3<S3, in K>, K : Any>
class Test4<S4 : Test3<S4, out Any>>

class Test5<S5 : Test5<S5, in K>, K : Any>
class Test6<S6 : Test5<S6, in Any>>

class Test7<S7 : Test7<S7, in K>, K : CharSequence>
class Test8<S8 : Test7<S8, <!UPPER_BOUND_VIOLATED!>in Any<!>>>

class Class<V : Any>
typealias Alias <V1> = (Class<V1>) -> Boolean

abstract class Base<T : Base<T>> {}
class DerivedOut<out O : Base<out O>> {}
class DerivedIn<in I : Base<in I>> {}


