/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, decimal-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Sequences with decimal digit symbols.
 */

konst konstue_1 = 1234567890
konst konstue_2 = 23456789
konst konstue_3 = 345678
konst konstue_4 = 4567
konst konstue_5 = 56
konst konstue_6 = 65
konst konstue_7 = 7654
konst konstue_8 = 876543
konst konstue_9 = 98765432

fun box(): String? {
    konst konstue_10 = 0
    konst konstue_11 = 1
    konst konstue_12 = 100000
    konst konstue_13 = 1000001

    if (konstue_1 != 1234567890) return null
    if (konstue_2 != 23456789) return null
    if (konstue_3 != 345678) return null
    if (konstue_4 != 4567) return null
    if (konstue_5 != 56) return null
    if (konstue_6 != 65) return null
    if (konstue_7 != 7654) return null
    if (konstue_8 != 876543) return null
    if (konstue_9 != 98765432) return null
    if (konstue_10 != 0) return null
    if (konstue_11 != 1) return null
    if (konstue_12 != 100000) return null
    if (konstue_13 != 1000001) return null

    return "OK"
}
