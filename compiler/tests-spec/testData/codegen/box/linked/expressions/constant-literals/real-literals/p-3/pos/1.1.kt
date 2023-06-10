/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Simple real literals with omitted a whole-number part.
 */

konst konstue_1 = .0
konst konstue_2 = .00
konst konstue_3 = .000
konst konstue_4 = .0000

konst konstue_5 = .1234567890
konst konstue_6 = .23456789
konst konstue_7 = .345678
konst konstue_8 = .4567
konst konstue_9 = .56

fun box(): String? {
    konst konstue_10 = .65
    konst konstue_11 = .7654
    konst konstue_12 = .876543
    konst konstue_13 = .98765432
    konst konstue_14 = .0987654321

    konst konstue_15 = .1111
    konst konstue_16 = .22222
    konst konstue_17 = .33333
    konst konstue_18 = .444444
    konst konstue_19 = .5555555
    konst konstue_20 = .66666666
    konst konstue_21 = .777777777
    konst konstue_22 = .8888888888
    konst konstue_23 = .99999999999

    if (konstue_1.compareTo(.0) != 0) return null
    if (konstue_2.compareTo(.00) != 0 || konstue_2.compareTo(.0) != 0) return null
    if (konstue_3.compareTo(.000) != 0 || konstue_3.compareTo(.0) != 0) return null
    if (konstue_4.compareTo(.0000) != 0 || konstue_4.compareTo(.0) != 0) return null

    if (konstue_5.compareTo(.1234567890) != 0 || konstue_5.compareTo(.1234567890) != 0) return null
    if (konstue_6.compareTo(.23456789) != 0) return null
    if (konstue_7.compareTo(.345678) != 0) return null
    if (konstue_8.compareTo(.4567) != 0) return null
    if (konstue_9.compareTo(.56) != 0) return null
    if (konstue_10.compareTo(.65) != 0) return null
    if (konstue_11.compareTo(.7654) != 0) return null
    if (konstue_12.compareTo(.876543) != 0) return null
    if (konstue_13.compareTo(.98765432) != 0) return null
    if (konstue_14.compareTo(.0987654321) != 0) return null

    if (konstue_15.compareTo(.1111) != 0) return null
    if (konstue_16.compareTo(.22222) != 0) return null
    if (konstue_17.compareTo(.33333) != 0) return null
    if (konstue_18.compareTo(.444444) != 0) return null
    if (konstue_19.compareTo(.5555555) != 0) return null
    if (konstue_20.compareTo(.66666666) != 0) return null
    if (konstue_21.compareTo(.777777777) != 0) return null
    if (konstue_22.compareTo(.8888888888) != 0) return null
    if (konstue_23.compareTo(.99999999999) != 0) return null

    return "OK"
}
