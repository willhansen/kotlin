// FIR_IDENTICAL
const konst iConst = 42
konst iVal = 42
fun iFun() = 42

annotation class Ann(konst x: Int)
annotation class Test1(konst x: Int = 42)
annotation class Test2(konst x: Int = iConst)
annotation class Test3(konst x: Int = 1 + iConst + 1)
annotation class Test4(konst x: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>iVal<!>)
annotation class Test5(konst x: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>1 + iVal + 1<!>)
annotation class Test6(konst x: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>iFun()<!>)
annotation class Test7(konst x: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>1 + iFun() + 1<!>)
