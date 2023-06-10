/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Sequences with binary digit symbols separated by underscores.
 */

konst konstue_1 = 0b1_110110100
konst konstue_2 = 0b1_______1011010
konst konstue_3 = 0b1_0_1_1_0_1
konst konstue_4 = 0b0_______1_______1_______0
konst konstue_5 = 0b1__________________________________________________________________________________________________1
konst konstue_6 = 0b0_______0

fun box(): String? {
    konst konstue_7 = 0B0_0
    konst konstue_8 = 0b1_______________________________________________________________________________________________________________________________________________________0
    konst konstue_9 = 0b1_00000000000000_1

    if (konstue_1 != 0b1110110100 || konstue_1 != 0B1_110110100 || konstue_1 != 948) return null
    if (konstue_2 != 0b11011010 || konstue_2 != 0b1_______1011010 || konstue_2 != 218) return null
    if (konstue_3 != 0b101101 || konstue_3 != 0b1_0_1_1_0_1 || konstue_3 != 45) return null
    if (konstue_4 != 0b0110 || konstue_4 != 0B0_______1_______1_______0 || konstue_4 != 6) return null
    if (konstue_5 != 0b11 || konstue_5 != 0b1__________________________________________________________________________________________________1 || konstue_5 != 3) return null
    if (konstue_6 != 0b0_______0 || konstue_6 != 0b00 || konstue_6 != 0b0 || konstue_6 != 0) return null
    if (konstue_7 != 0b0_0 || konstue_7 != 0b00 || konstue_7 != 0b0 || konstue_7 != 0) return null
    if (konstue_8 != 0b10 || konstue_8 != 0B1_______________________________________________________________________________________________________________________________________________________0 || konstue_8 != 2) return null
    if (konstue_9 != 0b1000000000000001 || konstue_9 != 0B1_00000000000000_1 || konstue_9 != 32769) return null

    return "OK"
}
