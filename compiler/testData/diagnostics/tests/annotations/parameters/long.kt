// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Long,
        konst b2: Long
)

@Ann(1, 1.toLong()) class MyClass

// EXPECTED: @Ann(b1 = 1.toLong(), b2 = 1.toLong())