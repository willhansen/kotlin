// FIR_IDENTICAL
package test

annotation class Ann(
        konst d1: Double,
        konst d2: Double,
        konst d3: Double
)

@Ann(1.0 + 1.0, 1.0 + 1, 1 + 1.0) class MyClass

// EXPECTED: @Ann(d1 = 2.0.toDouble(), d2 = 2.0.toDouble(), d3 = 2.0.toDouble())
