package test

annotation class Ann(
        konst b1: Short,
        konst b2: Short,
        konst b3: Short,
        konst b4: Short
)

@Ann(1, 1.toShort(), 32768.toShort(), <!CONSTANT_EXPECTED_TYPE_MISMATCH!>32768<!>) class MyClass

// EXPECTED: @Ann(b1 = 1.toShort(), b2 = 1.toShort(), b3 = -32768.toShort(), b4 = 32768)