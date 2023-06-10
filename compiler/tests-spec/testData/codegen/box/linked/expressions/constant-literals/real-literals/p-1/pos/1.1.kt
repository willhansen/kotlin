/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Simple real literals with a different whole-number part and fraction part.
 */

konst konstue_1 = 0.0
konst konstue_2 = 0.00
konst konstue_3 = 0.000
konst konstue_4 = 0.0000

konst konstue_5 = 00.0
konst konstue_6 = 000.00
konst konstue_7 = 0000.000

konst konstue_8 = 1.0
konst konstue_9 = 22.00
konst konstue_10 = 333.000
konst konstue_11 = 4444.0000
konst konstue_12 = 55555.0
konst konstue_13 = 666666.00
konst konstue_14 = 7777777.000
konst konstue_15 = 88888888.0000
konst konstue_16 = 999999999.0

konst konstue_17 = 0000000000.1234567890
konst konstue_18 = 123456789.23456789
konst konstue_19 = 2345678.345678
konst konstue_20 = 34567.4567
konst konstue_21 = 456.56

fun box(): String? {
    konst konstue_22 = 5.65
    konst konstue_23 = 654.7654
    konst konstue_24 = 76543.876543
    konst konstue_25 = 8765432.98765432
    konst konstue_26 = 987654321.0987654321

    konst konstue_27 = 0.1111
    konst konstue_28 = 1.22222
    konst konstue_29 = 9.33333
    konst konstue_30 = 9.444444
    konst konstue_31 = 8.5555555
    konst konstue_32 = 2.66666666
    konst konstue_33 = 3.777777777
    konst konstue_34 = 7.8888888888
    konst konstue_35 = 6.99999999999

    if (konstue_1.compareTo(0.0) != 0) return null
    if (konstue_2.compareTo(0.00) != 0 || konstue_2.compareTo(0.0) != 0) return null
    if (konstue_3.compareTo(0.000) != 0 || konstue_3.compareTo(0.000) != 0) return null
    if (konstue_4.compareTo(0.0000) != 0 || konstue_4.compareTo(0.0) != 0) return null
    if (konstue_5.compareTo(00.0) != 0 || konstue_5.compareTo(0.0) != 0) return null
    if (konstue_6.compareTo(000.000) != 0 || konstue_6.compareTo(0.0) != 0) return null
    if (konstue_7.compareTo(0000.000) != 0 || konstue_7.compareTo(0.0) != 0) return null

    if (konstue_8.compareTo(1.0) != 0) return null
    if (konstue_9.compareTo(22.00) != 0 || konstue_9.compareTo(22.0) != 0) return null
    if (konstue_10.compareTo(333.000) != 0 || konstue_10.compareTo(333.0) != 0) return null
    if (konstue_11.compareTo(4444.0000) != 0 || konstue_11.compareTo(4444.0) != 0) return null
    if (konstue_12.compareTo(55555.0) != 0) return null
    if (konstue_13.compareTo(666666.00) != 0 || konstue_13.compareTo(666666.0) != 0) return null
    if (konstue_14.compareTo(7777777.000) != 0 || konstue_14.compareTo(7777777.0) != 0) return null
    if (konstue_15.compareTo(88888888.0000) != 0 || konstue_15.compareTo(88888888.0) != 0) return null
    if (konstue_16.compareTo(999999999.0) != 0) return null

    if (konstue_17.compareTo(0000000000.1234567890) != 0 || konstue_17.compareTo(0.1234567890) != 0) return null
    if (konstue_18.compareTo(123456789.23456789) != 0) return null
    if (konstue_19.compareTo(2345678.345678) != 0) return null
    if (konstue_20.compareTo(34567.4567) != 0) return null
    if (konstue_21.compareTo(456.56) != 0) return null
    if (konstue_22.compareTo(5.65) != 0) return null
    if (konstue_23.compareTo(654.7654) != 0) return null
    if (konstue_24.compareTo(76543.876543) != 0) return null
    if (konstue_25.compareTo(8765432.98765432) != 0) return null
    if (konstue_26.compareTo(987654321.0987654321) != 0) return null

    if (konstue_27.compareTo(0.1111) != 0) return null
    if (konstue_28.compareTo(1.22222) != 0) return null
    if (konstue_29.compareTo(9.33333) != 0) return null
    if (konstue_30.compareTo(9.444444) != 0) return null
    if (konstue_31.compareTo(8.5555555) != 0) return null
    if (konstue_32.compareTo(2.66666666) != 0) return null
    if (konstue_33.compareTo(3.777777777) != 0) return null
    if (konstue_34.compareTo(7.8888888888) != 0) return null
    if (konstue_35.compareTo(6.99999999999) != 0) return null

    return "OK"
}
