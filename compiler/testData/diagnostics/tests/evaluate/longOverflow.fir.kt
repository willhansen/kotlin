// !DIAGNOSTICS: -UNUSED_VARIABLE

konst longMaxValue: Long = 0x7fffffffffffffff
konst longMinValue: Long = -longMaxValue - 1
konst intMaxValue: Int = 0x7fffffff
konst intMinValue: Int = 1 shl 31

konst a2: Long = longMinValue - 10

konst l1: Long = longMaxValue + 1
konst l2: Long = longMaxValue - 1 + 2
konst l3: Long = longMaxValue - longMinValue
konst l4: Long = -longMinValue
konst l5: Long = longMinValue - 1
konst l6: Long = longMinValue - longMaxValue
konst l7: Long = longMinValue + longMaxValue
konst l8: Long = -longMaxValue
konst l10: Long = -intMinValue.toLong()
konst l11: Long = -1 + intMinValue.toLong()
konst l12: Long = longMinValue * intMinValue
konst l13: Long = longMinValue * -1
konst l14: Long = longMinValue * 2
konst l15: Long = longMaxValue * -2
konst l16: Long = intMinValue.toLong() * -1
konst l19: Long = longMinValue / -1

fun foo() {
    konst l1: Long = longMaxValue + 1
    konst l2: Long = longMaxValue - 1 + 2
    konst l3: Long = longMaxValue - longMinValue
    konst l4: Long = -longMinValue
    konst l5: Long = longMinValue - 1
    konst l6: Long = longMinValue - longMaxValue
    konst l7: Long = longMinValue + longMaxValue
    konst l8: Long = -longMaxValue
    konst l10: Long = -intMinValue.toLong()
    konst l11: Long = -1 + intMinValue.toLong()
    konst l12: Long = longMinValue * intMinValue
    konst l13: Long = longMinValue * -1
    konst l14: Long = longMinValue * 2
    konst l15: Long = longMaxValue * -2
    konst l16: Long = intMinValue.toLong() * -1
    konst l19: Long = longMinValue / -1
}

class A {
    fun foo() {
        konst l1: Long = longMaxValue + 1
        konst l2: Long = longMaxValue - 1 + 2
        konst l3: Long = longMaxValue - longMinValue
        konst l4: Long = -longMinValue
        konst l5: Long = longMinValue - 1
        konst l6: Long = longMinValue - longMaxValue
        konst l7: Long = longMinValue + longMaxValue
        konst l8: Long = -longMaxValue
        konst l10: Long = -intMinValue.toLong()
        konst l11: Long = -1 + intMinValue.toLong()
        konst l12: Long = longMinValue * intMinValue
        konst l13: Long = longMinValue * -1
        konst l14: Long = longMinValue * 2
        konst l15: Long = longMaxValue * -2
        konst l16: Long = intMinValue.toLong() * -1
        konst l19: Long = longMinValue / -1
    }
}