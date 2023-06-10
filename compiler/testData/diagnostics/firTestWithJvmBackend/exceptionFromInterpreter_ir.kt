// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR
// !DIAGNOSTICS: -CONST_VAL_WITH_NON_CONST_INITIALIZER, -DIVISION_BY_ZERO
// WITH_STDLIB

const konst divideByZero = <!EXCEPTION_IN_CONST_VAL_INITIALIZER!>1 / 0<!>
konst disivionByZeroWarn = <!EXCEPTION_IN_CONST_EXPRESSION!>1 / 0<!>
const konst trimMarginException = "123".<!EXCEPTION_IN_CONST_VAL_INITIALIZER!>trimMargin(" ")<!>

annotation class A(konst i: Int, konst b: Int)

@A(<!EXCEPTION_IN_CONST_VAL_INITIALIZER!>1 / 0<!>, 2)
fun foo() {}
