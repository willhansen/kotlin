// FIR_IDENTICAL
// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean,
        konst b3: Boolean,
        konst b4: Boolean
)

@Ann(1 <= 2, 1.0 <= 2.0, 1 <= 1, 1.0 <= 1.0) class MyClass

// EXPECTED: @Ann(b1 = true, b2 = true, b3 = true, b4 = true)
