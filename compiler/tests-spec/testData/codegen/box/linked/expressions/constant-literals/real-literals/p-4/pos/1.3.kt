/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with an exponent mark and underscores in a whole-number part, a fraction part and an exponent part.
 */

konst konstue_1 = 0.0_0e1_0f
konst konstue_2 = 0.0__0e-0___0
konst konstue_3 = 0.0_0E-0__0_0F
konst konstue_4 = 0__0.0e0f
konst konstue_5 = 0_0_0.0_0E0_0
konst konstue_6 = 00_______________00.0_0e+0_0

konst konstue_7 = 2_2.0e1_0F
konst konstue_8 = 33__3.0e10__0
konst konstue_9 = 4_44____4.0E0______00F
konst konstue_10 = 5_________555_________5.0e-9
konst konstue_11 = 666_666.0__________________________________________________1E+2___________________________________________________________________0F
konst konstue_12 = 7777777.0_0e3_0
konst konstue_13 = 8888888_8.000e0f
konst konstue_14 = 9_______9______9_____9____9___9__9_9.0E-1

konst konstue_15 = 0_0_0_0_0_0_0_0_0_0.12345678e+90F
konst konstue_16 = 1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9e-0

fun box(): String? {
    konst konstue_17 = 234_5_678.345______________6e7_______8f
    konst konstue_18 = 3_456_7.45_6E7f
    konst konstue_19 = 456.5e0_6
    konst konstue_20 = 5.6_0E+05F
    konst konstue_21 = 6_54.76_5e-4
    konst konstue_22 = 7_6543.8E7654_3
    konst konstue_23 = 876543_____________2.9E+0_____________8765432f
    konst konstue_24 = 9_____________87654321.0e-9_8765432_____________1F

    konst konstue_25 = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000e000000000000000000000000000000000000000000000000000000000000000_0F
    konst konstue_26 = 0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0E-0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
    konst konstue_27 = 9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f

    if (konstue_1.compareTo(0.0_0e1_0f) != 0 || konstue_1.compareTo(0.0f) != 0) return null
    if (konstue_2.compareTo(0.0__0e-0___0) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_3.compareTo(0.0_0E-0__0_0F) != 0 || konstue_3.compareTo(0.0f) != 0) return null
    if (konstue_4.compareTo(0__0.0e0f) != 0 || konstue_4.compareTo(0.0) != 0) return null
    if (konstue_5.compareTo(0_0_0.0_0E0_0) != 0 || konstue_5.compareTo(0.0f) != 0) return null
    if (konstue_6.compareTo(00_______________00.0_0e+0_0) != 0 || konstue_6.compareTo(0.0f) != 0) return null

    if (konstue_7.compareTo(2_2.0e1_0F) != 0 || konstue_7.compareTo(2.19999994E11f) != 0) return null
    if (konstue_8.compareTo(33__3.0e10__0) != 0 || konstue_8.compareTo(3.33E102) != 0) return null
    if (konstue_9.compareTo(4_44____4.0E0______00F) != 0 || konstue_9.compareTo(4444.0f) != 0) return null
    if (konstue_10.compareTo(5_________555_________5.0e-9) != 0 || konstue_10.compareTo(5.5555E-5) != 0) return null
    if (konstue_11.compareTo(666_666.0__________________________________________________1E+2___________________________________________________________________0F) != 0 || konstue_11.compareTo(6.66666E25F) != 0) return null
    if (konstue_12.compareTo(7777777.0_0e3_0) != 0 || konstue_12.compareTo(7.777777E36) != 0) return null
    if (konstue_13.compareTo(8888888_8.000e0f) != 0 || konstue_13.compareTo(8.8888888E7) != 0) return null
    if (konstue_14.compareTo(9_______9______9_____9____9___9__9_9.0E-1) != 0 || konstue_14.compareTo(9999999.9) != 0) return null

    if (konstue_15.compareTo(0_0_0_0_0_0_0_0_0_0.12345678e+90F) != 0 || konstue_15.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_16.compareTo(1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9e-0) != 0 || konstue_16.compareTo(1.234567892345679E8) != 0) return null
    if (konstue_17.compareTo(234_5_678.345______________6e7_______8f) != 0 || konstue_17.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_18.compareTo(3_456_7.45_6E7f) != 0 || konstue_18.compareTo(3.45674547E11F) != 0) return null
    if (konstue_19.compareTo(456.5e0_6) != 0 || konstue_19.compareTo(4.565E8f) != 0) return null
    if (konstue_20.compareTo(5.6_0E+05F) != 0 || konstue_20.compareTo(560000.0F) != 0) return null
    if (konstue_21.compareTo(6_54.76_5e-4) != 0 || konstue_21.compareTo(0.0654765) != 0) return null
    if (konstue_22.compareTo(7_6543.8E7654_3) != 0 || konstue_22.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_23.compareTo(876543_____________2.9E+0_____________8765432f) != 0 || konstue_23.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_24.compareTo(9_____________87654321.0e-9_8765432_____________1F) != 0 || konstue_24.compareTo(0.0) != 0) return null
    if (konstue_25.compareTo(000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000e000000000000000000000000000000000000000000000000000000000000000_0F) != 0 || konstue_25.compareTo(0.0) != 0) return null
    if (konstue_26.compareTo(0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0E-0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000) != 0 || konstue_26.compareTo(0.0) != 0) return null
    if (konstue_27.compareTo(9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f) != 0 || konstue_27.compareTo(Float.POSITIVE_INFINITY) != 0) return null

    return "OK"
}
