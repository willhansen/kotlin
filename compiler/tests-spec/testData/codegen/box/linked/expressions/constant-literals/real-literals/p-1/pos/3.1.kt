/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: Simple real literals suffixed by f/F (the float suffix) with a different whole-number part and fraction part.
 */

konst konstue_1 = 0.0f
konst konstue_2 = 0.00F
konst konstue_3 = 0.000f
konst konstue_4 = 0.0000F

konst konstue_5 = 00.0f
konst konstue_6 = 000.00f
konst konstue_7 = 0000.000F

konst konstue_8 = 1.0F
konst konstue_9 = 22.00F
konst konstue_10 = 333.000F
konst konstue_11 = 4444.0000f
konst konstue_12 = 55555.0f
konst konstue_13 = 666666.00f
konst konstue_14 = 7777777.000F
konst konstue_15 = 88888888.0000f
konst konstue_16 = 999999999.0F

konst konstue_17 = 0000000000.1234567890f
konst konstue_18 = 123456789.23456789f
konst konstue_19 = 2345678.345678F
konst konstue_20 = 34567.4567f
konst konstue_21 = 456.56F

fun box(): String? {
    konst konstue_22 = 5.65F
    konst konstue_23 = 654.7654f
    konst konstue_24 = 76543.876543f
    konst konstue_25 = 8765432.98765432F
    konst konstue_26 = 987654321.0987654321f

    konst konstue_27 = 0.1111f
    konst konstue_28 = 1.22222f
    konst konstue_29 = 9.33333F
    konst konstue_30 = 9.444444F
    konst konstue_31 = 8.5555555F
    konst konstue_32 = 2.66666666F
    konst konstue_33 = 3.777777777F
    konst konstue_34 = 7.8888888888f
    konst konstue_35 = 6.99999999999f

    if (konstue_1.compareTo(0.0F) != 0) return null
    if (konstue_2.compareTo(0.00f) != 0 || konstue_2.compareTo(0.0f) != 0) return null
    if (konstue_3.compareTo(0.000F) != 0 || konstue_3.compareTo(0.000f) != 0) return null
    if (konstue_4.compareTo(0.0000f) != 0 || konstue_4.compareTo(0.0F) != 0) return null
    if (konstue_5.compareTo(00.0f) != 0 || konstue_5.compareTo(0.0F) != 0) return null
    if (konstue_6.compareTo(000.000f) != 0 || konstue_6.compareTo(0.0F) != 0) return null
    if (konstue_7.compareTo(0000.000f) != 0 || konstue_7.compareTo(0.0f) != 0) return null

    if (konstue_8.compareTo(1.0F) != 0) return null
    if (konstue_9.compareTo(22.00F) != 0 || konstue_9.compareTo(22.0F) != 0) return null
    if (konstue_10.compareTo(333.000F) != 0 || konstue_10.compareTo(333.0f) != 0) return null
    if (konstue_11.compareTo(4444.0000f) != 0 || konstue_11.compareTo(4444.0f) != 0) return null
    if (konstue_12.compareTo(55555.0F) != 0) return null
    if (konstue_13.compareTo(666666.00F) != 0 || konstue_13.compareTo(666666.0f) != 0) return null
    if (konstue_14.compareTo(7777777.000f) != 0 || konstue_14.compareTo(7777777.0F) != 0) return null
    if (konstue_15.compareTo(88888888.0000f) != 0 || konstue_15.compareTo(88888888.0F) != 0) return null
    if (konstue_16.compareTo(999999999.0f) != 0) return null

    if (konstue_17.compareTo(0000000000.1234567890f) != 0 || konstue_17.compareTo(0.1234567890F) != 0) return null
    if (konstue_18.compareTo(123456789.23456789F) != 0) return null
    if (konstue_19.compareTo(2345678.345678F) != 0) return null
    if (konstue_20.compareTo(34567.4567f) != 0) return null
    if (konstue_21.compareTo(456.56f) != 0) return null
    if (konstue_22.compareTo(5.65F) != 0) return null
    if (konstue_23.compareTo(654.7654f) != 0) return null
    if (konstue_24.compareTo(76543.876543F) != 0) return null
    if (konstue_25.compareTo(8765432.98765432F) != 0) return null
    if (konstue_26.compareTo(987654321.0987654321f) != 0) return null

    if (konstue_27.compareTo(0.1111f) != 0) return null
    if (konstue_28.compareTo(1.22222f) != 0) return null
    if (konstue_29.compareTo(9.33333f) != 0) return null
    if (konstue_30.compareTo(9.444444f) != 0) return null
    if (konstue_31.compareTo(8.5555555F) != 0) return null
    if (konstue_32.compareTo(2.66666666F) != 0) return null
    if (konstue_33.compareTo(3.777777777f) != 0) return null
    if (konstue_34.compareTo(7.8888888888F) != 0) return null
    if (konstue_35.compareTo(6.99999999999f) != 0) return null

    return "OK"
}
