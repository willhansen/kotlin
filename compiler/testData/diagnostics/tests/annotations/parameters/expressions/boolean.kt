// FIR_IDENTICAL
// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean,
        konst b3: Boolean
)

@Ann(true and false, false or true, true xor false) class MyClass

// EXPECTED: @Ann(b1 = false, b2 = true, b3 = true)
