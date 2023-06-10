// FIR_IDENTICAL
// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean,
        konst b3: Boolean,
        konst b4: Boolean,
        konst b5: Boolean,
        konst b6: Boolean
)

konst a = 1
konst b = 2

@Ann(1 > 2, 1.0 > 2.0, <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>2 > a<!>, <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>b > a<!>, 'b' > 'a', "a" > "b") class MyClass

// EXPECTED: @Ann(b1 = false, b2 = false, b3 = true, b4 = true, b5 = true, b6 = false)
