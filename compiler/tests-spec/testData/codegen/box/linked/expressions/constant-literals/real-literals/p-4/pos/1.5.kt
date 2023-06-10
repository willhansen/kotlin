/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: Real literals with an omitted fraction part and underscores in a whole-number part, a fraction part and an exponent part.
 */

konst konstue_1 = 0_0F
konst konstue_2 = 0_0E-0_0F
konst konstue_3 = 0_0E-0_0
konst konstue_4 = 0_0____0f
konst konstue_5 = 0_0____0e-0f
konst konstue_6 = 0_0_0_0F
konst konstue_7 = 0_0_0_0E-0_0_0_0F
konst konstue_8 = 0000000000000000000_______________0000000000000000000f
konst konstue_9 = 0000000000000000000_______________0000000000000000000e+0f
konst konstue_10 = 0000000000000000000_______________0000000000000000000E-0

konst konstue_11 = 2___2e-2___2f
konst konstue_12 = 33_3E0_0F
konst konstue_13 = 4_444E-4_444f
konst konstue_14 = 55_5_55F
konst konstue_15 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666f
konst konstue_16 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666E-10
konst konstue_17 = 7_7_7_7_7_7_7f
konst konstue_18 = 8888888________8e-9000000_0
konst konstue_19 = 9________9_______9______9_____9____9___9__9_9F

konst konstue_20 = 1__2_3__4____5_____6__7_89f
konst konstue_21 = 2__34567e8
konst konstue_22 = 345_6E+9_7F

fun box(): String? {
    konst konstue_23 = 45_____________________________________________________________6E-12313413_4
    konst konstue_24 = 5_______________________________________________________________________________________________________________________________________________________________________________________5f
    konst konstue_25 = 6__________________________________________________54F
    konst konstue_26 = 76_5___4e3___________33333333
    konst konstue_27 = 876543_____________________________________________________________2f
    konst konstue_28 = 9_8__7654__3_21F

    konst konstue_29 = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0__0F
    konst konstue_30 = 0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f
    konst konstue_31 = 33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E-1_0_0

    if (konstue_1.compareTo(0_0F) != 0 || konstue_1.compareTo(0.0) != 0) return null
    if (konstue_2.compareTo(0_0E-0_0F) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_3.compareTo(0_0E-0_0) != 0 || konstue_3.compareTo(0.0f) != 0) return null
    if (konstue_4.compareTo(0_0____0f) != 0 || konstue_4.compareTo(0.0) != 0) return null
    if (konstue_5.compareTo(0_0____0e-0f) != 0 || konstue_5.compareTo(0.0f) != 0) return null
    if (konstue_6.compareTo(0_0_0_0F) != 0 || konstue_6.compareTo(0.0) != 0) return null

    if (konstue_7.compareTo(0_0_0_0E-0_0_0_0F) != 0 || konstue_7.compareTo(0.0f) != 0) return null
    if (konstue_8.compareTo(0000000000000000000_______________0000000000000000000f) != 0 || konstue_8.compareTo(0.0) != 0) return null
    if (konstue_9.compareTo(0000000000000000000_______________0000000000000000000e+0f) != 0 || konstue_9.compareTo(0.0F) != 0) return null
    if (konstue_10.compareTo(0000000000000000000_______________0000000000000000000E-0) != 0 || konstue_10.compareTo(0.0) != 0) return null
    if (konstue_11.compareTo(2___2e-2___2f) != 0 || konstue_11.compareTo(2.2E-21f) != 0) return null
    if (konstue_12.compareTo(33_3E0_0F) != 0 || konstue_12.compareTo(333.0F) != 0) return null
    if (konstue_13.compareTo(4_444E-4_444f) != 0 || konstue_13.compareTo(0.0) != 0) return null
    if (konstue_14.compareTo(55_5_55F) != 0 || konstue_14.compareTo(55555.0F) != 0) return null

    if (konstue_15.compareTo(666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666f) != 0 || konstue_15.compareTo(666666.0f) != 0) return null
    if (konstue_16.compareTo(666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666E-10) != 0 || konstue_16.compareTo(6.66666E-5) != 0) return null
    if (konstue_17.compareTo(7_7_7_7_7_7_7f) != 0 || konstue_17.compareTo(7777777.0F) != 0) return null
    if (konstue_18.compareTo(8888888________8e-9000000_0) != 0 || konstue_18.compareTo(0.0) != 0) return null
    if (konstue_19.compareTo(9________9_______9______9_____9____9___9__9_9F) != 0 || konstue_19.compareTo(1.0E9f) != 0) return null
    if (konstue_20.compareTo(1__2_3__4____5_____6__7_89f) != 0 || konstue_20.compareTo(1.23456792E8F) != 0) return null
    if (konstue_21.compareTo(2__34567e8) != 0 || konstue_21.compareTo(2.34567E13) != 0) return null
    if (konstue_22.compareTo(345_6E+9_7F) != 0 || konstue_22.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_23.compareTo(45_____________________________________________________________6E-12313413_4) != 0 || konstue_23.compareTo(0.0) != 0) return null
    if (konstue_24.compareTo(5_______________________________________________________________________________________________________________________________________________________________________________________5f) != 0 || konstue_24.compareTo(55.0F) != 0) return null
    if (konstue_25.compareTo(6__________________________________________________54F) != 0 || konstue_25.compareTo(654.0f) != 0) return null
    if (konstue_26.compareTo(76_5___4e3___________33333333) != 0 || konstue_26.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (konstue_27.compareTo(876543_____________________________________________________________2f) != 0 || konstue_27.compareTo(8765432.0F) != 0) return null
    if (konstue_28.compareTo(9_8__7654__3_21F) != 0 || konstue_28.compareTo(9.8765434E8f) != 0) return null
    if (konstue_29.compareTo(000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0__0F) != 0 || konstue_29.compareTo(0.0) != 0) return null
    if (konstue_30.compareTo(0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f) != 0 || konstue_30.compareTo(0.0) != 0) return null
    if (konstue_31.compareTo(33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E-1_0_0) != 0 || konstue_31.compareTo(3.3333333333333334E-13) != 0) return null

    return "OK"
}
