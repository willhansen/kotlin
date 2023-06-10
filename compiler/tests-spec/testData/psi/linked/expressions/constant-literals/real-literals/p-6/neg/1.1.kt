/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 6 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Real literals suffixed by not supported d/D (a double suffix).
 */

konst konstue = 0.0d
konst konstue = 0.00d
konst konstue = 0000.000D

konst konstue = 1.0d
konst konstue = 22.00D

konst konstue = 0.0e0d
konst konstue = 0.0e-00D
konst konstue = 0.0E+0000D
konst konstue = 0000.000E-000d

konst konstue = 1.0E+1d
konst konstue = 333.000e-333d
konst konstue = 123456789.23456789E+123456789D

konst konstue = .0d
konst konstue = .0000d
konst konstue = .1234567890D
konst konstue = .9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999123456789912345678991234567899123456789912345678991234567899D

konst konstue = 0d
konst konstue = 00000000000000000000000000000000000000D
konst konstue = 4444d
konst konstue = 987654321d
konst konstue = 0e0D
konst konstue = 00e00D
konst konstue = 000E-10d
konst konstue = 0000e+00000000000d
konst konstue = 00000000000000000000000000000000000000E1D

konst konstue = 00e-00D
konst konstue = 1e1d
konst konstue = 333e-00000000000d
konst konstue = 88888888e1234567890D

konst konstue = 0.0__0___0D
konst konstue = 0_0_0_0E-0_0_0_0d
konst konstue = .0_0E+0__0_0D
konst konstue = 0_0_0.0_0E0_0d
konst konstue = 666_666.0_____________________________________________________________________________________________________________________0d
konst konstue = 9_______9______9_____9____9___9__9_9.0E-1D
konst konstue = .0e-9_8765432_____________1D
konst konstue = 45_____________________________________________________________6E-12313413_4d
