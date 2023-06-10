// FILE: a.kt
package boundsWithSubstitutors
    open class A<T>
    class B<X : A<X>>()

    class C : A<C>()

    konst a = B<C>()
    konst a1 = B<<!UPPER_BOUND_VIOLATED!>Int<!>>()

    class X<A, B : A>()

    konst b = X<Any, X<A<C>, C>>()
    konst b0 = X<Any, <!UPPER_BOUND_VIOLATED!>Any?<!>>()
    konst b1 = X<Any, X<A<C>, <!UPPER_BOUND_VIOLATED!>String<!>>>()

// FILE: b.kt
  open class A {}
  open class B<T : A>()

  class Pair<A, B>

  abstract class C<T : B<<!UPPER_BOUND_VIOLATED!>Int<!>>, X :  (B<<!UPPER_BOUND_VIOLATED!>Char<!>>) -> Pair<B<<!UPPER_BOUND_VIOLATED!>Any<!>>, B<A>>>() : B<<!UPPER_BOUND_VIOLATED!>Any<!>>() { // 2 errors
    konst a = B<<!UPPER_BOUND_VIOLATED!>Char<!>>() // error

    abstract konst x :  (B<<!UPPER_BOUND_VIOLATED!>Char<!>>) -> B<<!UPPER_BOUND_VIOLATED!>Any<!>>
  }


fun test() {
    foo<<!UPPER_BOUND_VIOLATED!>Int?<!>>()
    foo<Int>()
    bar<Int?>()
    bar<Int>()
    bar<<!UPPER_BOUND_VIOLATED!>Double?<!>>()
    bar<<!UPPER_BOUND_VIOLATED!>Double<!>>()
    1.buzz<<!UPPER_BOUND_VIOLATED!>Double<!>>()
}

fun <T : Any> foo() {}
fun <T : Int?> bar() {}
fun <T : <!FINAL_UPPER_BOUND!>Int<!>> Int.buzz() : Unit {}
