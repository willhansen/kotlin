/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Hexadecimal integer literals with inkonstid or double prefixes.
 */

konst konstue = 00x876543
konst konstue = 00l0xf876L543
konst konstue = 0F0l0xf876L543
konst konstue = 0F0l0xf876L543
konst konstue = 000000000000000000000000000000000000000000000000000000xAcccccccccA
konst konstue = 00xA45

konst konstue = 0XXX
konst konstue = 0xXx
konst konstue = 0xX11111
konst konstue = 0x0X0
konst konstue = 0x000x
konst konstue = 0000000000x

konst konstue = 90X7654
konst konstue = 09x98765432
konst konstue = 0lX0000000
konst konstue = 0Fx0000001000000
konst konstue = 0e10fxEeEeEeEe
konst konstue = 0EXAAAAAAAA
konst konstue = 200x

konst konstue = 0A
konst konstue = 0z
