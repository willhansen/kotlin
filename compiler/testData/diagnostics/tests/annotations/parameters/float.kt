// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Float,
        konst b2: Float
)

@Ann(1.toFloat(), 1.0.toFloat()) class MyClass

// EXPECTED: @Ann(b1 = 1.0.toFloat(), b2 = 1.0.toFloat())