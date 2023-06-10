/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with omitted a whole-number part.
 */

konst konstue_1 = .0F
konst konstue_2 = .00F
konst konstue_3 = .000F
konst konstue_4 = .0000f

konst konstue_5 = .1234567890f
konst konstue_6 = .23456789f
konst konstue_7 = .345678F
konst konstue_8 = .4567f
konst konstue_9 = .56F

fun box(): String? {
    konst konstue_10 = .65F
    konst konstue_11 = .7654f
    konst konstue_12 = .876543f
    konst konstue_13 = .98765432F
    konst konstue_14 = .0987654321f

    konst konstue_15 = .1111f
    konst konstue_16 = .22222f
    konst konstue_17 = .33333F
    konst konstue_18 = .444444F
    konst konstue_19 = .5555555F
    konst konstue_20 = .66666666F
    konst konstue_21 = .777777777F
    konst konstue_22 = .8888888888f
    konst konstue_23 = .99999999999f

    if (konstue_1.compareTo(.0F) != 0) return null
    if (konstue_2.compareTo(.00F) != 0 || konstue_2.compareTo(.0F) != 0) return null
    if (konstue_3.compareTo(.000F) != 0 || konstue_3.compareTo(.0f) != 0) return null
    if (konstue_4.compareTo(.0000f) != 0 || konstue_4.compareTo(.0f) != 0) return null

    if (konstue_5.compareTo(.1234567890f) != 0 || konstue_5.compareTo(.1234567890F) != 0) return null
    if (konstue_6.compareTo(.23456789F) != 0) return null
    if (konstue_7.compareTo(.345678F) != 0) return null
    if (konstue_8.compareTo(.4567f) != 0) return null
    if (konstue_9.compareTo(.56f) != 0) return null
    if (konstue_10.compareTo(.65F) != 0) return null
    if (konstue_11.compareTo(.7654f) != 0) return null
    if (konstue_12.compareTo(.876543F) != 0) return null
    if (konstue_13.compareTo(.98765432F) != 0) return null
    if (konstue_14.compareTo(.0987654321f) != 0) return null

    if (konstue_15.compareTo(.1111f) != 0) return null
    if (konstue_16.compareTo(.22222f) != 0) return null
    if (konstue_17.compareTo(.33333f) != 0) return null
    if (konstue_18.compareTo(.444444f) != 0) return null
    if (konstue_19.compareTo(.5555555F) != 0) return null
    if (konstue_20.compareTo(.66666666F) != 0) return null
    if (konstue_21.compareTo(.777777777f) != 0) return null
    if (konstue_22.compareTo(.8888888888F) != 0) return null
    if (konstue_23.compareTo(.99999999999f) != 0) return null

    return "OK"
}
