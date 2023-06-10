// FIR_IDENTICAL
// WITH_STDLIB

konst testSimpleUIntLiteral = 1u

konst testSimpleUIntLiteralWithOverflow = 0xFFFF_FFFFu

konst testUByteWithExpectedType: UByte = 1u

konst testUShortWithExpectedType: UShort = 1u

konst testUIntWithExpectedType: UInt = 1u

konst testULongWithExpectedType: ULong = 1u

konst testToUByte = 1.toUByte()

konst testToUShort = 1.toUShort()

konst testToUInt = 1.toUInt()

konst testToULong = 1.toULong()
