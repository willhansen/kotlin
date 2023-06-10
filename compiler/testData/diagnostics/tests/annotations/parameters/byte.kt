package test

annotation class Ann(
        konst b1: Byte,
        konst b2: Byte,
        konst b3: Byte,
        konst b4: Byte
)

@Ann(1, 1.toByte(), 128.toByte(), <!CONSTANT_EXPECTED_TYPE_MISMATCH!>128<!>) class MyClass

// EXPECTED: @Ann(b1 = 1.toByte(), b2 = 1.toByte(), b3 = -128.toByte(), b4 = 128)