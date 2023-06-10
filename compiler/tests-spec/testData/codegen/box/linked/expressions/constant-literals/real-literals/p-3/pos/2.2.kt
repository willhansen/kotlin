/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark.
 */

konst konstue_1 = 0e0
konst konstue_2 = 00e00
konst konstue_3 = 000E-10
konst konstue_4 = 0000e+00000000000
konst konstue_5 = 00000000000000000000000000000000000000E1

konst konstue_6 = 1e1
konst konstue_7 = 22E-1
konst konstue_8 = 333e-00000000000
konst konstue_9 = 4444E-99999999999999999
konst konstue_10 = 55555e10
konst konstue_11 = 666666E00010
konst konstue_12 = 7777777e09090909090
konst konstue_13 = 88888888e1234567890
konst konstue_14 = 999999999E1234567890

konst konstue_15 = 123456789e987654321
konst konstue_16 = 2345678E0
konst konstue_17 = 34567E+010
konst konstue_18 = 456e-09876543210
konst konstue_19 = 5e505
konst konstue_20 = 654e5
konst konstue_21 = 76543E-91823
konst konstue_22 = 8765432e+90
konst konstue_23 = 987654321e-1

fun box(): String? {
    if (konstue_1.compareTo(0e0) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_2.compareTo(00e00) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_3.compareTo(000E-10) != 0 || konstue_3.compareTo(0.0) != 0) return null
    if (konstue_4.compareTo(0000e+00000000000) != 0 || konstue_4.compareTo(0.0) != 0) return null
    if (konstue_5.compareTo(00000000000000000000000000000000000000E1) != 0 || konstue_5.compareTo(0.0) != 0) return null

    if (konstue_6.compareTo(1e1) != 0 || konstue_6.compareTo(10.0) != 0) return null
    if (konstue_7.compareTo(22E-1) != 0 || konstue_7.compareTo(2.2) != 0) return null
    if (konstue_8.compareTo(333e-00000000000) != 0 || konstue_8.compareTo(333.0) != 0) return null
    if (konstue_9.compareTo(4444E-99999999999999999) != 0 || konstue_9.compareTo(0.0) != 0) return null
    if (konstue_10.compareTo(55555e10) != 0 || konstue_10.compareTo(5.5555E14) != 0) return null
    if (konstue_11.compareTo(666666E00010) != 0 || konstue_11.compareTo(6.66666e15) != 0) return null
    if (konstue_12.compareTo(7777777e09090909090) != 0 || konstue_12.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_13.compareTo(88888888e1234567890) != 0 || konstue_13.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_14.compareTo(999999999E1234567890) != 0 || konstue_14.compareTo(Double.POSITIVE_INFINITY) != 0) return null

    if (konstue_15.compareTo(123456789e987654321) != 0 || konstue_15.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_16.compareTo(2345678E0) != 0 || konstue_16.compareTo(2345678.0) != 0) return null
    if (konstue_17.compareTo(34567E+010) != 0 || konstue_17.compareTo(3.4567E14) != 0) return null
    if (konstue_18.compareTo(456e-09876543210) != 0 || konstue_18.compareTo(0.0) != 0) return null
    if (konstue_19.compareTo(5e505) != 0 || konstue_19.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_20.compareTo(654e5) != 0 || konstue_20.compareTo(6.54E7) != 0) return null
    if (konstue_21.compareTo(76543E-91823) != 0 || konstue_21.compareTo(0.0) != 0) return null
    if (konstue_22.compareTo(8765432e+90) != 0 || konstue_22.compareTo(8.765432E96) != 0) return null
    if (konstue_23.compareTo(987654321e-1) != 0 || konstue_23.compareTo(9.87654321E7) != 0) return null

    return "OK"
}
