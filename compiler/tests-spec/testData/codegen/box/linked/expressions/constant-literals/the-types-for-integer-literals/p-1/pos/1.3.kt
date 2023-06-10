/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Binary integer literals with long literal mark.
 */

konst konstue_1 = 0b0L

konst konstue_2 = 0B1111111L
konst konstue_3 = 0b10000000L
konst konstue_4 = -0b10000000L
konst konstue_5 = -0b10000001L

konst konstue_6 = 0B111111111111111L
konst konstue_7 = 0b1000000000000000L
konst konstue_8 = -0b1000000000000000L
konst konstue_9 = -0b1000000000000001L

konst konstue_10 = 0b1111111111111111111111111111111L
konst konstue_11 = 0B10000000000000000000000000000000L
konst konstue_12 = -0B10000000000000000000000000000000L
konst konstue_13 = -0b10000000000000000000000000000001L

fun box(): String? {
    konst konstue_14 = 0X7FFFFFFFFFFFFFFFL
    konst konstue_15 = -0X7FFFFFFFFFFFFFFFL

    if (konstue_1 != 0b0L) return null
    if (konstue_2 != 0B1111111L) return null
    if (konstue_3 != 0b10000000L) return null
    if (konstue_4 != -0b10000000L) return null
    if (konstue_5 != -0b10000001L) return null
    if (konstue_6 != 0B111111111111111L) return null
    if (konstue_7 != 0b1000000000000000L) return null
    if (konstue_8 != -0b1000000000000000L) return null
    if (konstue_9 != -0b1000000000000001L) return null
    if (konstue_10 != 0b1111111111111111111111111111111L) return null
    if (konstue_11 != 0B10000000000000000000000000000000L) return null
    if (konstue_12 != -0B10000000000000000000000000000000L) return null
    if (konstue_13 != -0b10000000000000000000000000000001L) return null
    if (konstue_14 != 0b111111111111111111111111111111111111111111111111111111111111111L) return null
    if (konstue_15 != -0B111111111111111111111111111111111111111111111111111111111111111L) return null

    return "OK"
}
