/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Sequences with hexadecimal digit symbols separated by underscores.
 */

konst konstue_1 = 0x1_23a567b90
konst konstue_2 = 0XF_______34a6789f
konst konstue_3 = 0x3_c_c_c_7_8
konst konstue_4 = 0X4_______b_______6_______d
konst konstue_5 = 0X5__________________________________________________________________________________________________f
konst konstue_6 = 0x0_______0

fun box(): String? {
    konst konstue_7 = 0X0_0
    konst konstue_8 = 0xa_______________________________________________________________________________________________________________________________________________________0
    konst konstue_9 = 0x1_00000000000000_1

    if (konstue_1 != 0x123a567b90 || konstue_1 != 0x1_23a567b90 || konstue_1 != 78288157584) return null
    if (konstue_2 != 0XF34a6789f || konstue_2 != 0xF_______34a6789f || konstue_2 != 65307834527) return null
    if (konstue_3 != 0x3ccc78 || konstue_3 != 0x3_c_c_c_7_8 || konstue_3 != 3984504) return null
    if (konstue_4 != 0X4b6d || konstue_4 != 0x4_______b_______6_______d || konstue_4 != 19309) return null
    if (konstue_5 != 0X5f || konstue_5 != 0X5__________________________________________________________________________________________________f || konstue_5 != 95) return null
    if (konstue_6 != 0x0_______0 || konstue_6 != 0x00 || konstue_6 != 0x0 || konstue_6 != 0) return null
    if (konstue_7 != 0x0_0 || konstue_7 != 0X00 || konstue_7 != 0x0 || konstue_7 != 0) return null
    if (konstue_8 != 0xa0 || konstue_8 != 0xa_______________________________________________________________________________________________________________________________________________________0 || konstue_8 != 160) return null
    if (konstue_9 != 0x1000000000000001 || konstue_9 != 0X1_00000000000000_1 || konstue_9 != 1152921504606846977) return null

    return "OK"
}
