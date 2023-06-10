// FIR_IDENTICAL
package test

annotation class Ann(
        konst p1: Int,
        konst p2: Int,
        konst p3: Int,
        konst p4: Int,
        konst p5: Int
)

@Ann(1.plus(1), 1.minus(1), 1.times(1), 1.div(1), 1.rem(1)) class MyClass

// EXPECTED: @Ann(p1 = 2, p2 = 0, p3 = 1, p4 = 1, p5 = 0)
