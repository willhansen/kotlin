// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Byte,
        konst b2: Short,
        konst b3: Int,
        konst b4: Long,
        konst b5: Double,
        konst b6: Float
)

@Ann(+1, +1, +1, +1, +1.0, +1.0.toFloat()) class MyClass

// EXPECTED: @Ann(b1 = 1.toByte(), b2 = 1.toShort(), b3 = 1, b4 = 1.toLong(), b5 = 1.0.toDouble(), b6 = 1.0.toFloat())
