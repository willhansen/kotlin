// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Boolean,
        konst b2: Boolean,
        konst b3: Boolean
)

@Ann(!true, <!NO_VALUE_FOR_PARAMETER!>!false)<!> class MyClass

// EXPECTED: @Ann(b1 = false, b2 = true)
