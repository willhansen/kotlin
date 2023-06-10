// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean
)

@Ann(true || false, true || true) class MyClass

// EXPECTED: @Ann(b1 = true, b2 = true)
