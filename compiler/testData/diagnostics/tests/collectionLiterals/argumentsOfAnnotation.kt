annotation class Foo(konst a: IntArray, konst b: Array<String>, konst c: FloatArray)

@Foo([1], ["/"], [1f])
fun test1() {}

@Foo([], [], [])
fun test2() {}

@Foo([<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1f<!>], <!TYPE_MISMATCH!>[' ']<!>, [<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>])
fun test3() {}

@Foo(c = [1f], b = [""], a = [1])
fun test4() {}

@Foo([1 + 2], ["Hello, " + "Kotlin"], [<!DIVISION_BY_ZERO!>1 / 0f<!>])
fun test5() {}

const konst ONE = 1
konst two = 2

@Foo([ONE], [], [])
fun test6() {}

@Foo(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>[<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>ONE + two<!>]<!>, [], [])
fun test7() {}

@Foo(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>[<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>two<!>]<!>, [], [])
fun test8() {}
