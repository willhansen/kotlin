konst nonConst = 1

const konst constConst = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>nonConst * nonConst + 2<!>

annotation class Ann(konst x: Int, konst y: String)

@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>nonConst<!>, <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"${nonConst}"<!>)
fun foo1() {}

@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>nonConst + constConst<!>, "${constConst}")
fun foo2() {}

annotation class ArrayAnn(konst x: IntArray)

@ArrayAnn(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(1, constConst, <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>nonConst<!>)<!>)
fun foo3() {}
