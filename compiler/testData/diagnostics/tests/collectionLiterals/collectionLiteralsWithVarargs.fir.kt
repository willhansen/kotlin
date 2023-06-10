// !LANGUAGE: +ProhibitAssigningSingleElementsToVarargsInNamedForm

annotation class Ann1(vararg konst a: String = [])
annotation class Ann2(vararg konst a: Int = [1, 2])
annotation class Ann3(vararg konst a: Float = [1f])
annotation class Ann4(vararg konst a: String = ["/"])

annotation class Ann5(vararg konst a: Ann4 = [])
annotation class Ann6(vararg konst a: Ann4 = [Ann4(*["a", "b"])])

annotation class Ann7(vararg konst a: Long = [1L, null, ""])

@Ann1(*[])
fun test1_0() {}

@Ann1(*["a", "b"])
fun test1_1() {}

@Ann1(*["a", 1, null])
fun test1_2() {}

@Ann2(*[])
fun test2() {}

@Ann3(a = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION!>[0f, <!DIVISION_BY_ZERO!>1 / 0f<!>]<!>)
fun test3() {}

@Ann5(Ann4(*["/"]))
fun test5() {}

@Ann6(*[])
fun test6() {}

annotation class AnnArray(konst a: Array<String>)

@AnnArray(<!NON_VARARG_SPREAD!>*<!>["/"])
fun testArray() {}

@Ann1([""])
fun testVararg() {}
