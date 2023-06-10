/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Sequences with hexadecimal digit symbols.
 */

konst konstue_1 = 0x1234567890
konst konstue_2 = 0X23456789
konst konstue_3 = 0x345678
konst konstue_7 = 0X7654
konst konstue_8 = 0x876543
konst konstue_9 = 0x98765432

konst konstue_10 = 0X0
konst konstue_11 = 0x1

konst konstue_12 = 0x100000
konst konstue_13 = 0X1000001

konst konstue_14 = 0X0000000
konst konstue_16 = 0x00000010

konst konstue_17 = 0xABCDEF
konst konstue_18 = 0Xabcdef
konst konstue_19 = 0xAbcD
konst konstue_23 = 0XAAAAAAAA
konst konstue_24 = 0xcDf
konst konstue_25 = 0xAcccccccccA

konst konstue_26 = 0x0123456789ABCDEF
konst konstue_27 = 0x0123456789abcdef
konst konstue_28 = 0XAA00AA
konst konstue_29 = 0xBc12eF
konst konstue_30 = 0xa0
konst konstue_31 = 0XE1
konst konstue_32 = 0xE1eE2eE3eE4e
konst konstue_33 = 0XAAAAAA000000
konst konstue_34 = 0xcDf091

fun box(): String? {
    konst konstue_4 = 0X4567
    konst konstue_5 = 0X56
    konst konstue_6 = 0x65
    konst konstue_15 = 0x0000001000000
    konst konstue_20 = 0Xa
    konst konstue_21 = 0xE
    konst konstue_22 = 0xEeEeEeEe
    konst konstue_35 = 0xAcccc0000cccccA
    konst konstue_36 = 0X0000000A
    konst konstue_37 = 0xe0000000
    konst konstue_38 = 0x0000000D0000000
    konst konstue_39 = 0xA0A

    if (konstue_1 != 0x1234567890 || konstue_1 != 78187493520) return null
    if (konstue_2 != 0X23456789 || konstue_2 != 591751049) return null
    if (konstue_3 != 0x345678 || konstue_3 != 3430008) return null
    if (konstue_4 != 0X4567 || konstue_4 != 17767) return null
    if (konstue_5 != 0X56 || konstue_5 != 86) return null
    if (konstue_6 != 0x65 || konstue_6 != 101) return null
    if (konstue_7 != 0X7654 || konstue_7 != 30292) return null
    if (konstue_8 != 0x876543 || konstue_8 != 8873283) return null
    if (konstue_9 != 0x98765432 || konstue_9 != 2557891634) return null
    if (konstue_10 != 0X0 || konstue_10 != 0) return null
    if (konstue_11 != 0x1 || konstue_11 != 1) return null
    if (konstue_12 != 0x100000 || konstue_12 != 1048576) return null
    if (konstue_13 != 0X1000001 || konstue_13 != 16777217) return null
    if (konstue_14 != 0X0000000 || konstue_14 != 0) return null
    if (konstue_15 != 0x0000001000000 || konstue_15 != 16777216) return null
    if (konstue_16 != 0x00000010 || konstue_16 != 16) return null
    if (konstue_17 != 0xABCDEF || konstue_17 != 11259375) return null
    if (konstue_18 != 0Xabcdef || konstue_18 != 11259375) return null
    if (konstue_19 != 0xAbcD || konstue_19 != 43981) return null
    if (konstue_20 != 0Xa || konstue_20 != 10) return null
    if (konstue_21 != 0xE || konstue_21 != 14) return null
    if (konstue_22 != 0xEeEeEeEe || konstue_22 != 4008636142) return null
    if (konstue_23 != 0XAAAAAAAA || konstue_23 != 2863311530) return null
    if (konstue_24 != 0xcDf || konstue_24 != 3295) return null
    if (konstue_25 != 0xAcccccccccA || konstue_25 != 11874725579978) return null
    if (konstue_26 != 0x0123456789ABCDEF || konstue_26 != 81985529216486895) return null
    if (konstue_27 != 0x0123456789abcdef || konstue_27 != 81985529216486895) return null
    if (konstue_28 != 0XAA00AA || konstue_28 != 11141290) return null
    if (konstue_29 != 0xBc12eF || konstue_29 != 12325615) return null
    if (konstue_30 != 0xa0 || konstue_30 != 160) return null
    if (konstue_31 != 0XE1 || konstue_31 != 225) return null
    if (konstue_32 != 0xE1eE2eE3eE4e || konstue_32 != 248413105155662) return null
    if (konstue_33 != 0XAAAAAA000000 || konstue_33 != 187649973288960) return null
    if (konstue_34 != 0xcDf091 || konstue_34 != 13496465) return null
    if (konstue_35 != 0xAcccc0000cccccA || konstue_35 != 778221136013741258) return null
    if (konstue_36 != 0X0000000A || konstue_36 != 10) return null
    if (konstue_37 != 0xe0000000 || konstue_37 != 3758096384) return null
    if (konstue_38 != 0x0000000D0000000 || konstue_38 != 3489660928) return null
    if (konstue_39 != 0xA0A || konstue_39 != 2570) return null

    return "OK"
}
