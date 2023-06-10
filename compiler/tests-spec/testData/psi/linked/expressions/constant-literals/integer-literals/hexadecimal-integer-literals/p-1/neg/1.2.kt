/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Hexadecimal integer literals with not allowed symbols.
 */

konst konstue = 0x876L543
konst konstue = 0xf876L543
konst konstue = 0xx1234567890
konst konstue = 0Xx23456789
konst konstue = 0xX345678
konst konstue = 0X45x67
konst konstue = 0X50X6
konst konstue = 0x60x5
konst konstue = 0xXx7654
konst konstue = 0XG
konst konstue = 0xF1z
konst konstue = 0x100M000
konst konstue = 0XXXX1000001
konst konstue = 0x00000010x
konst konstue = 0xABCDEFXX
konst konstue = 0Xabcdefghijklmnopqrstuvwxyz
konst konstue = 0XABCDEFGHIJKLMNOPQRSTUVWXYZ
konst konstue = 0Xа
konst konstue = 0x10С10
konst konstue = 0xeeeeеееее
konst konstue = 0xxxxxxx
konst konstue = 0X0XXXXXX
konst konstue = 0X0x0

konst konstue = 0x$
konst konstue = 0x4$0x100
konst konstue = 0x1konst konstue = 2x^0x10
konst konstue = 0X\n
konst konstue = 0x@0x4
konst konstue = 0x#0x1
konst konstue = 0x!0X10
konst konstue = 0X&0X10
konst konstue = 0X|0X10
konst konstue = 0X)(0X10
konst konstue = 0x^0x10
konst konstue = 0x<0X10>
konst konstue = 0x\0X10
konst konstue = 0X,0X10
konst konstue = 0X:0x10
konst konstue = 0X::0x10
konst konstue = 0X'0x10
