// FIR_IDENTICAL
package test

annotation class Ann(
        konst p1: Int,
        konst p2: Int,
        konst p3: Int
)

@Ann(1.toInt().plus(1), 1.minus(1.toInt()), 1.toInt().times(1.toInt())) class MyClass

// EXPECTED: @Ann(p1 = 2, p2 = 0, p3 = 1)
