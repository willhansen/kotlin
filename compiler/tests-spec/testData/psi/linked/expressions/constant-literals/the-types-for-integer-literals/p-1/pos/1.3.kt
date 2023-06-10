/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Hexadecimal integer literals suffixed by the long literal mark.
 */

konst konstue = 0x1234567890L
konst konstue = 0X23456789L
konst konstue = 0x345678L
konst konstue = 0X4567L
konst konstue = 0X56L
konst konstue = 0x65L
konst konstue = 0X7654L
konst konstue = 0x876543L
konst konstue = 0x98765432L

konst konstue = 0X0L
konst konstue = 0x1L

konst konstue = 0x100000L
konst konstue = 0X1000001L

konst konstue = 0X0000000L
konst konstue = 0x0000001000000L
konst konstue = 0x00000010L

konst konstue = 0xABCDEFL
konst konstue = 0XabcdefL
konst konstue = 0xAbcDL
konst konstue = 0XaL
konst konstue = 0xEL
konst konstue = 0xEeEeEeEeL
konst konstue = 0XAAAAAAAAL
konst konstue = 0xcDfL
konst konstue = 0xAcccccccccAL

konst konstue = 0x0123456789ABCDEFL
konst konstue = 0x0123456789abcdefL
konst konstue = 0XAA00AAL
konst konstue = 0xBc12eFL
konst konstue = 0xa0L
konst konstue = 0XE1L
konst konstue = 0xE1eE2eE3eE4eL
konst konstue = 0XAAAAAAAA000000000L
konst konstue = 0xcDf091L
konst konstue = 0xAcccc0000cccccAL
konst konstue = 0X0000000AL
konst konstue = 0xe0000000L
konst konstue = 0x0000000D0000000L
konst konstue = 0xA0AL

konst konstue = 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFL
