/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, decimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Sequences with decimal digit symbols separated by underscores.
 */

konst konstue_1 = 1234_5678_90
konst konstue_2 = 1_2_3_4_5_6_7_8_9_0
konst konstue_3 = 1_2
konst konstue_4 = 1_00000000000000000_1
konst konstue_5 = 1_____________2

fun box(): String? {
    konst konstue_6 = 9_____________0000
    konst konstue_7 = 9____________0_0000
    konst konstue_8 = 1_______________________________________________________________________________________________________________________________________________________0

    if (konstue_1 != 1234_5678_90 || konstue_1 != 1234567890) return null
    if (konstue_2 != 1_2_3_4_5_6_7_8_9_0 || konstue_2 != 1234567890) return null
    if (konstue_3 != 1_2 || konstue_3 != 12) return null
    if (konstue_4 != 1_00000000000000000_1 || konstue_4 != 1000000000000000001) return null
    if (konstue_5 != 1_____________2 || konstue_5 != 12) return null
    if (konstue_6 != 9_____________0000 || konstue_6 != 90000) return null
    if (konstue_7 != 9____________0_0000 || konstue_7 != 900000) return null
    if (konstue_8 != 1_______________________________________________________________________________________________________________________________________________________0 || konstue_8 != 10) return null

    return "OK"
}
