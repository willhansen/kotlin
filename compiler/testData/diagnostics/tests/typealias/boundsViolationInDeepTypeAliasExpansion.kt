// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

class TColl<T, C : Collection<T>>

typealias TC<T1, T2> = TColl<T1, T2>
typealias TC2<T1, T2> = TC<T1, T2>

fun test1(x: TC2<Number, Collection<Number>>) {}
fun test2(x: TC2<Number, Collection<Int>>) {}
fun test3(x: TC2<Number, List<Int>>) {}
fun test4(x: <!UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION!>TC2<Number, List<Any>><!>) {}

konst test5 = TC2<Number, Collection<Number>>()
konst test6 = TC2<Number, Collection<Int>>()
konst test7 = TC2<Number, List<Int>>()
konst test8 = TC2<Number, <!UPPER_BOUND_VIOLATED, UPPER_BOUND_VIOLATED!>List<Any><!>>()
