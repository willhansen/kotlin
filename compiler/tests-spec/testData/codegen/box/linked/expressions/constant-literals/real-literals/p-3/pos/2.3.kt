/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark, suffixed by f/F (float suffix).
 */

konst konstue_1 = 0e0f
konst konstue_2 = 00e00F
konst konstue_3 = 000E-10f
konst konstue_4 = 0000e+00000000000f
konst konstue_5 = 00000000000000000000000000000000000000E1F

konst konstue_6 = 1e1F
konst konstue_7 = 22E-1f
konst konstue_8 = 333e-00000000000F
konst konstue_9 = 4444E-99999999999999999f
konst konstue_10 = 55555e10f
konst konstue_11 = 666666E00010F
konst konstue_12 = 7777777e09090909090F
konst konstue_13 = 88888888e1234567890F
konst konstue_14 = 999999999E1234567890f

konst konstue_15 = 123456789e987654321F
konst konstue_16 = 2345678E0f
konst konstue_17 = 34567E+010f
konst konstue_18 = 456e-09876543210F
konst konstue_19 = 5e505f
konst konstue_20 = 654e5F
konst konstue_21 = 76543E-91823f
konst konstue_22 = 8765432e+90F
konst konstue_23 = 987654321e-1f

fun box(): String? {
    if (konstue_1.compareTo(0e0f) != 0 || konstue_2.compareTo(0.0f) != 0) return null
    if (konstue_2.compareTo(00e00F) != 0 || konstue_2.compareTo(0.0F) != 0) return null
    if (konstue_3.compareTo(000E-10F) != 0 || konstue_3.compareTo(0.0f) != 0) return null
    if (konstue_4.compareTo(0000e+00000000000F) != 0 || konstue_4.compareTo(0.0f) != 0) return null
    if (konstue_5.compareTo(00000000000000000000000000000000000000E1f) != 0 || konstue_5.compareTo(0.0f) != 0) return null

    if (konstue_6.compareTo(1e1f) != 0 || konstue_6.compareTo(10.0F) != 0) return null
    if (konstue_7.compareTo(22E-1F) != 0 || konstue_7.compareTo(2.2f) != 0) return null
    if (konstue_8.compareTo(333e-00000000000F) != 0 || konstue_8.compareTo(333.0F) != 0) return null
    if (konstue_9.compareTo(4444E-99999999999999999F) != 0 || konstue_9.compareTo(0.0f) != 0) return null
    if (konstue_10.compareTo(55555e10F) != 0 || konstue_10.compareTo(5.5555E14f) != 0) return null
    if (konstue_11.compareTo(666666E00010F) != 0 || konstue_11.compareTo(6.66666e15f) != 0) return null
    if (konstue_12.compareTo(7777777e09090909090f) != 0 || konstue_12.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_13.compareTo(88888888e1234567890F) != 0 || konstue_13.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_14.compareTo(999999999E1234567890f) != 0 || konstue_14.compareTo(Float.POSITIVE_INFINITY) != 0) return null

    if (konstue_15.compareTo(123456789e987654321f) != 0 || konstue_15.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_16.compareTo(2345678E0F) != 0 || konstue_16.compareTo(2345678.0F) != 0) return null
    if (konstue_17.compareTo(34567E+010F) != 0 || konstue_17.compareTo(3.4567E14f) != 0) return null
    if (konstue_18.compareTo(456e-09876543210F) != 0 || konstue_18.compareTo(0.0f) != 0) return null
    if (konstue_19.compareTo(5e505f) != 0 || konstue_19.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (konstue_20.compareTo(654e5f) != 0 || konstue_20.compareTo(6.54E7F) != 0) return null
    if (konstue_21.compareTo(76543E-91823f) != 0 || konstue_21.compareTo(0.0f) != 0) return null
    if (konstue_22.compareTo(8765432e+90F) != 0 || konstue_22.compareTo(8.765432E96F) != 0) return null
    if (konstue_23.compareTo(987654321e-1f) != 0 || konstue_23.compareTo(9.87654321E7F) != 0) return null

    return "OK"
}
