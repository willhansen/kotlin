/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Sequences with binary digit symbols.
 */

konst konstue_1 = 0b1110001100
konst konstue_2 = 0B11000110
konst konstue_3 = 0b100011
konst konstue_7 = 0B1000
konst konstue_8 = 0b110001
konst konstue_9 = 0b01100011

konst konstue_10 = 0B0
konst konstue_11 = 0b1

konst konstue_12 = 0b100000
konst konstue_13 = 0B1000001

konst konstue_14 = 0B0000000
konst konstue_16 = 0b00000010

konst konstue_17 = 0b001101
konst konstue_18 = 0B001101
konst konstue_19 = 0b0011
konst konstue_23 = 0B00000000
konst konstue_24 = 0b111
konst konstue_25 = 0b01111111110

konst konstue_26 = 0b0111000110001101
konst konstue_27 = 0b0111000110001101
konst konstue_28 = 0B000000
konst konstue_29 = 0b011101
konst konstue_30 = 0b00
konst konstue_31 = 0B01
konst konstue_32 = 0b010010010000
konst konstue_33 = 0B000000000000
konst konstue_34 = 0b111001

fun box(): String? {
    konst konstue_4 = 0B0001
    konst konstue_5 = 0B00
    konst konstue_6 = 0b00
    konst konstue_15 = 0b0000001000000
    konst konstue_20 = 0B0
    konst konstue_21 = 0b0
    konst konstue_22 = 0b00000000
    konst konstue_35 = 0b011110000111110
    konst konstue_36 = 0B00000000
    konst konstue_37 = 0b00000000
    konst konstue_38 = 0b000000010000000
    konst konstue_39 = 0b000

    if (konstue_1 != 0b1110001100 || konstue_1 != 908) return null
    if (konstue_2 != 0B11000110 || konstue_2 != 198) return null
    if (konstue_3 != 0b100011 || konstue_3 != 35) return null
    if (konstue_4 != 0B0001 || konstue_4 != 1) return null
    if (konstue_5 != 0B00 || konstue_5 != 0) return null
    if (konstue_6 != 0b00 || konstue_6 != 0) return null
    if (konstue_7 != 0B1000 || konstue_7 != 8) return null
    if (konstue_8 != 0b110001 || konstue_8 != 49) return null
    if (konstue_9 != 0b01100011 || konstue_9 != 99) return null
    if (konstue_10 != 0B0 || konstue_10 != 0) return null
    if (konstue_11 != 0b1 || konstue_11 != 1) return null
    if (konstue_12 != 0b100000 || konstue_12 != 32) return null
    if (konstue_13 != 0B1000001 || konstue_13 != 65) return null
    if (konstue_14 != 0B0000000 || konstue_14 != 0) return null
    if (konstue_15 != 0b0000001000000 || konstue_15 != 64) return null
    if (konstue_16 != 0b00000010 || konstue_16 != 2) return null
    if (konstue_17 != 0b001101 || konstue_17 != 13) return null
    if (konstue_18 != 0B001101 || konstue_18 != 13) return null
    if (konstue_19 != 0b0011 || konstue_19 != 3) return null
    if (konstue_20 != 0B0 || konstue_20 != 0) return null
    if (konstue_21 != 0b0 || konstue_21 != 0) return null
    if (konstue_22 != 0b00000000 || konstue_22 != 0) return null
    if (konstue_23 != 0B00000000 || konstue_23 != 0) return null
    if (konstue_24 != 0b111 || konstue_24 != 7) return null
    if (konstue_25 != 0b01111111110 || konstue_25 != 1022) return null
    if (konstue_26 != 0b0111000110001101 || konstue_26 != 29069) return null
    if (konstue_27 != 0b0111000110001101 || konstue_27 != 29069) return null
    if (konstue_28 != 0B000000 || konstue_28 != 0) return null
    if (konstue_29 != 0b011101 || konstue_29 != 29) return null
    if (konstue_30 != 0b00 || konstue_30 != 0) return null
    if (konstue_31 != 0B01 || konstue_31 != 1) return null
    if (konstue_32 != 0b010010010000 || konstue_32 != 1168) return null
    if (konstue_33 != 0B000000000000 || konstue_33 != 0) return null
    if (konstue_34 != 0b111001 || konstue_34 != 57) return null
    if (konstue_35 != 0b011110000111110 || konstue_35 != 15422) return null
    if (konstue_36 != 0B00000000 || konstue_36 != 0) return null
    if (konstue_37 != 0b00000000 || konstue_37 != 0) return null
    if (konstue_38 != 0b000000010000000 || konstue_38 != 128) return null
    if (konstue_39 != 0b000 || konstue_39 != 0) return null

    return "OK"
}
