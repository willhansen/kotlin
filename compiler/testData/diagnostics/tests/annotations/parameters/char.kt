// FIR_IDENTICAL
package test

annotation class Ann(
        konst b1: Char,
        konst b2: Char,
        konst b3: Int,
        konst b4: Long,
        konst b5: Byte,
        konst b6: Short,
        konst b7: Double,
        konst b8: Float
)

@Ann('c', 99.toChar(), 'c'.<!DEPRECATION!>toInt<!>(), 'c'.<!DEPRECATION!>toLong<!>(), 'c'.<!DEPRECATION!>toByte<!>(), 'c'.<!DEPRECATION!>toShort<!>(), 'c'.<!DEPRECATION!>toDouble<!>(), 'c'.<!DEPRECATION!>toFloat<!>()) class MyClass

// EXPECTED: @Ann(b1 = \u0063 ('c'), b2 = \u0063 ('c'), b3 = 99, b4 = 99.toLong(), b5 = 99.toByte(), b6 = 99.toShort(), b7 = 99.0.toDouble(), b8 = 99.0.toFloat())