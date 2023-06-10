// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean,
        konst b3: Boolean,
        konst b4: Boolean,
        konst b5: Boolean
)

@Ann(1 != 2, 1.0 != 2.0, 'b' != 'a', "a" != "b", "a" != "a") class MyClass

// EXPECTED: @Ann(b1 = true, b2 = true, b3 = true, b4 = true, b5 = false)
