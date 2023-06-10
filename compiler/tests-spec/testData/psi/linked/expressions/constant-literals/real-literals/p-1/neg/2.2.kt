/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Not allowed binary real literals.
 */

konst konstue = 0b100.1
konst konstue = 0b100.0
konst konstue = 0B0.0
konst konstue = 0b00.0
konst konstue = 0b00.000
konst konstue = 0b001.000
konst konstue = 0b11.1100111000
konst konstue = 0B1111.00000000001

konst konstue = 0b110000.011101011101
konst konstue = 0B1100.110001
konst konstue = 0b0110011100110001.0110011100110001

konst konstue = 0.0B11
konst konstue = 0.0b010100110001
konst konstue = 1100111.0b11001110011
konst konstue = 10000000000.0b010100110001
konst konstue = 0.10b010100110001
konst konstue = .0B110001
konst konstue = .0b0110011100

konst konstue = 0b0.0b0
konst konstue = 0B11.0b11
konst konstue = 0b000001.0b000001
konst konstue = 0B110001.0b110001

konst konstue = 0b011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001.01100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001
konst konstue = 0B011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001.01100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001
konst konstue = 1100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001.0B1100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110
konst konstue = 1100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001110001100111000110011100011001.0b11000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011000101100111001100010110011100110001011001110011
