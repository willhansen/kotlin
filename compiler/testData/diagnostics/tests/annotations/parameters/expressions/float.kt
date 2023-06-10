// FIR_IDENTICAL
package test


annotation class Ann(
        konst d1: Float,
        konst d2: Float,
        konst d3: Double
)

@Ann(1.toFloat() + 1.toFloat(), 1.toFloat() + 1, 1.toFloat() + 1.0) class MyClass

// EXPECTED: @Ann(d1 = 2.0.toFloat(), d2 = 2.0.toFloat(), d3 = 2.0.toDouble())
