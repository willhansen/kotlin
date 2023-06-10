/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Real literals with an omitted whole-number part and underscores in a whole-number part, a fraction part and an exponent part.
 */

konst konstue_1 = .0_0
konst konstue_2 = .0_0f
konst konstue_3 = .0_0e-0_0
konst konstue_4 = .0_0e0_0F
konst konstue_5 = .0__0F
konst konstue_6 = .0_0E+0__0_0F

konst konstue_7 = .0e0f
konst konstue_8 = .0_0E0_0

konst konstue_9 = .0e1_0F
konst konstue_10 = .0e10__0
konst konstue_11 = .00______00F
konst konstue_12 = .0___9
konst konstue_13 = .0__________________________________________________12___________________________________________________________________0F
konst konstue_14 = .0_0e+3_0
konst konstue_15 = .000e0f
konst konstue_16 = .9_______9______9_____9____9___9__9_90E-1

konst konstue_17 = .12345678_90
konst konstue_18 = .1_2_3_4_5_6_7_8_9_0
konst konstue_19 = .345______________6e-7_______8f
konst konstue_20 = .45_67f
konst konstue_21 = .5e+0_6

fun box(): String? {
    konst konstue_22 = .6_0______________05F
    konst konstue_23 = .76_5e4
    konst konstue_24 = .8E7654_3
    konst konstue_25 = .9E0_____________8765432f
    konst konstue_26 = .09_8765432_____________1F

    konst konstue_27 = .000000000000000000000000e-000000000000000000000000000000000000000000000000000000000000000_0F
    konst konstue_28 = .00___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
    konst konstue_29 = .33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f

    if (konstue_1.compareTo(.0_0) != 0 || konstue_1.compareTo(0.0) != 0) return null
    if (konstue_2.compareTo(.0_0f) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_3.compareTo(.0_0e-0_0) != 0 || konstue_3.compareTo(0.0f) != 0) return null
    if (konstue_4.compareTo(.0_0e0_0F) != 0 || konstue_4.compareTo(0.0) != 0) return null
    if (konstue_5.compareTo(.0__0F) != 0 || konstue_5.compareTo(0.0f) != 0) return null
    if (konstue_6.compareTo(.0_0E+0__0_0F) != 0 || konstue_6.compareTo(0.0) != 0) return null

    if (konstue_7.compareTo(.0e0f) != 0 || konstue_7.compareTo(0.0f) != 0) return null
    if (konstue_8.compareTo(.0_0E0_0) != 0 || konstue_8.compareTo(0.0) != 0) return null
    if (konstue_9.compareTo(.0e1_0F) != 0 || konstue_9.compareTo(0.0F) != 0) return null
    if (konstue_10.compareTo(.0e10__0) != 0 || konstue_10.compareTo(0.0) != 0) return null
    if (konstue_11.compareTo(.00______00F) != 0 || konstue_11.compareTo(0.0F) != 0) return null
    if (konstue_12.compareTo(.0___9) != 0 || konstue_12.compareTo(0.09) != 0) return null
    if (konstue_13.compareTo(.0__________________________________________________12___________________________________________________________________0F) != 0 || konstue_13.compareTo(0.012f) != 0) return null
    if (konstue_14.compareTo(.0_0e+3_0) != 0 || konstue_14.compareTo(0.0) != 0) return null

    if (konstue_15.compareTo(.000e0f) != 0 || konstue_15.compareTo(0.0) != 0) return null
    if (konstue_16.compareTo(.9_______9______9_____9____9___9__9_90E-1) != 0 || konstue_16.compareTo(0.099999999) != 0) return null
    if (konstue_17.compareTo(.12345678_90) != 0 || konstue_17.compareTo(0.123456789) != 0) return null
    if (konstue_18.compareTo(.1_2_3_4_5_6_7_8_9_0) != 0 || konstue_18.compareTo(0.123456789) != 0) return null
    if (konstue_19.compareTo(.345______________6e-7_______8f) != 0 || konstue_19.compareTo(0.0) != 0) return null
    if (konstue_20.compareTo(.45_67f) != 0 || konstue_20.compareTo(0.4567F) != 0) return null
    if (konstue_21.compareTo(.5e+0_6) != 0 || konstue_21.compareTo(500000.0) != 0) return null
    if (konstue_22.compareTo(.6_0______________05F) != 0 || konstue_22.compareTo(0.6005f) != 0) return null
    if (konstue_23.compareTo(.76_5e4) != 0 || konstue_23.compareTo(7650.0) != 0) return null
    if (konstue_24.compareTo(.8E7654_3) != 0 || konstue_24.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_25.compareTo(.9E0_____________8765432f) != 0 || konstue_25.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_26.compareTo(.09_8765432_____________1F) != 0 || konstue_26.compareTo(0.09876543F) != 0) return null
    if (konstue_27.compareTo(.000000000000000000000000e-000000000000000000000000000000000000000000000000000000000000000_0F) != 0 || konstue_27.compareTo(0.0) != 0) return null
    if (konstue_28.compareTo(.00___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000) != 0 || konstue_28.compareTo(0.0) != 0) return null
    if (konstue_29.compareTo(.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f) != 0 || konstue_29.compareTo(3.3333334E32F) != 0) return null

    return "OK"
}
