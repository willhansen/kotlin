// TARGET_BACKEND: JVM_IR

annotation class Ann1(konst konstue: UByte = 41u)
annotation class Ann2(konst konstue: UShort = 42u)
annotation class Ann3(konst konstue: UInt = 43u)
annotation class Ann4(konst konstue: ULong = 44u)

// 1 default=\(byte\)41
// 1 default=\(short\)42
// 1 default=43
// 1 default=44L