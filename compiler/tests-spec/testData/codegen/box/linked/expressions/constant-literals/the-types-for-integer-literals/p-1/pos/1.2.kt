/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Hexadecimal integer literals with long literal mark.
 */

konst konstue_1 = 0x0L

konst konstue_2 = 0x7FL
konst konstue_3 = 0X80L
konst konstue_4 = -0X80L
konst konstue_5 = -0x81L

konst konstue_6 = 0x7FFFL
konst konstue_7 = 0x8000L
konst konstue_8 = -0x8000L
konst konstue_9 = -0x8001L

konst konstue_10 = 0x7FFFFFFFL
konst konstue_11 = 0x80000000L
konst konstue_12 = -0x80000000L
konst konstue_13 = -0x80000001L

fun box(): String? {
    konst konstue_14 = 0X7FFFFFFFFFFFFFFFL
    konst konstue_15 = -0X7FFFFFFFFFFFFFFFL

    if (konstue_1 != 0x0L) return null
    if (konstue_2 != 0x7FL) return null
    if (konstue_3 != 0X80L) return null
    if (konstue_4 != -0X80L) return null
    if (konstue_5 != -0x81L) return null
    if (konstue_6 != 0x7FFFL) return null
    if (konstue_7 != 0x8000L) return null
    if (konstue_8 != -0x8000L) return null
    if (konstue_9 != -0x8001L) return null
    if (konstue_10 != 0x7FFFFFFFL) return null
    if (konstue_11 != 0x80000000L) return null
    if (konstue_12 != -0x80000000L) return null
    if (konstue_13 != -0x80000001L) return null
    if (konstue_14 != 0X7FFFFFFFFFFFFFFFL) return null
    if (konstue_15 != -0X7FFFFFFFFFFFFFFFL) return null

    return "OK"
}
