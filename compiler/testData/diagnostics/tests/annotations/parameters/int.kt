package test

annotation class Ann(
        konst b1: Int,
        konst b2: Int,
        konst b3: Int,
        konst b4: Int
)

@Ann(1, 1.toInt(), 2147483648.toInt(), <!CONSTANT_EXPECTED_TYPE_MISMATCH!>2147483648<!>) class MyClass

// EXPECTED: @Ann(b1 = 1, b2 = 1, b3 = -2147483648, b4 = 2147483648.toLong())