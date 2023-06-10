/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Decimal integer literals with long literal mark.
 */

konst konstue_1 = 0L

konst konstue_2 = 127L
konst konstue_3 = 128L
konst konstue_4 = -128L
konst konstue_5 = -129L

konst konstue_6 = 32767L
konst konstue_7 = 32768L
konst konstue_8 = -32768L
konst konstue_9 = -32769L

konst konstue_10 = 2147483647L
konst konstue_11 = 2147483648L
konst konstue_12 = -2147483648L
konst konstue_13 = -2147483649L

fun box(): String? {
    konst konstue_14 = 9223372036854775807L
    konst konstue_15 = -9223372036854775807L

    if (konstue_1 != 0L) return null
    if (konstue_2 != 127L) return null
    if (konstue_3 != 128L) return null
    if (konstue_4 != -128L) return null
    if (konstue_5 != -129L) return null
    if (konstue_6 != 32767L) return null
    if (konstue_7 != 32768L) return null
    if (konstue_8 != -32768L) return null
    if (konstue_9 != -32769L) return null
    if (konstue_10 != 2147483647L) return null
    if (konstue_11 != 2147483648L) return null
    if (konstue_12 != -2147483648L) return null
    if (konstue_13 != -2147483649L) return null
    if (konstue_14 != 9223372036854775807L) return null
    if (konstue_15 != -9223372036854775807L) return null

    return "OK"
}
