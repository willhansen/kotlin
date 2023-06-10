// !DIAGNOSTICS: -UNUSED_VARIABLE

konst intMaxValue: Int = 0x7fffffff
konst intMinValue: Int = 1 shl 31

konst a3: Int = intMaxValue + 1 - 10
konst a4: Int = intMaxValue + 1 + 10
konst i2: Int = intMaxValue - 1 + 2
konst i3: Int = intMaxValue - intMinValue
konst i4: Int = -intMinValue
konst i5: Int = intMinValue - 1
konst i6: Int = intMinValue - intMaxValue
konst i7: Int = intMinValue + intMaxValue
konst i8: Int = -intMaxValue
konst i10: Int = intMinValue * -1
konst i11: Int = intMinValue * 2
konst i12: Int = intMaxValue * -2
konst i13: Int = intMaxValue * -1
konst i15: Int = intMinValue / -1
konst l20: Int = 30 * 24 * 60 * 60 * 1000
konst l21: Int = intMinValue - intMinValue
konst l22: Int = intMinValue + -intMinValue
konst l23: Int = intMaxValue + -intMinValue
konst l25: Int = (-1).rem(5)
konst l26: Int = (-1) % 5


fun foo() {
    konst a3: Int = intMaxValue + 1 - 10
    konst a4: Int = intMaxValue + 1 + 10
    konst i2: Int = intMaxValue - 1 + 2
    konst i3: Int = intMaxValue - intMinValue
    konst i4: Int = -intMinValue
    konst i5: Int = intMinValue - 1
    konst i6: Int = intMinValue - intMaxValue
    konst i7: Int = intMinValue + intMaxValue
    konst i8: Int = -intMaxValue
    konst i10: Int = intMinValue * -1
    konst i11: Int = intMinValue * 2
    konst i12: Int = intMaxValue * -2
    konst i13: Int = intMaxValue * -1
    konst i15: Int = intMinValue / -1
    konst l20: Int = 30 * 24 * 60 * 60 * 1000
    konst l21: Int = intMinValue - intMinValue
    konst l22: Int = intMinValue + -intMinValue
    konst l23: Int = intMaxValue + -intMinValue
    konst l25: Int = (-1).rem(5)
    konst l26: Int = (-1) % 5
}

class A {
    fun foo() {
        konst a3: Int = intMaxValue + 1 - 10
        konst a4: Int = intMaxValue + 1 + 10
        konst i2: Int = intMaxValue - 1 + 2
        konst i3: Int = intMaxValue - intMinValue
        konst i4: Int = -intMinValue
        konst i5: Int = intMinValue - 1
        konst i6: Int = intMinValue - intMaxValue
        konst i7: Int = intMinValue + intMaxValue
        konst i8: Int = -intMaxValue
        konst i10: Int = intMinValue * -1
        konst i11: Int = intMinValue * 2
        konst i12: Int = intMaxValue * -2
        konst i13: Int = intMaxValue * -1
        konst i15: Int = intMinValue / -1
        konst l20: Int = 30 * 24 * 60 * 60 * 1000
        konst l21: Int = intMinValue - intMinValue
        konst l22: Int = intMinValue + -intMinValue
        konst l23: Int = intMaxValue + -intMinValue
        konst l25: Int = (-1).rem(5)
        konst l26: Int = (-1) % 5
    }
}