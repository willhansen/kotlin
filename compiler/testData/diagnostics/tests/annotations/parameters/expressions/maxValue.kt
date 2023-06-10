package test

annotation class Ann(
        konst p1: Byte,
        konst p2: Short,
        konst p3: Int,
        konst p4: Int,
        konst p5: Long,
        konst p6: Long
)

@Ann(
    p1 = <!TYPE_MISMATCH!>java.lang.Byte.MAX_VALUE + 1<!>,
    p2 = <!TYPE_MISMATCH!>java.lang.Short.MAX_VALUE + 1<!>,
    p3 = <!INTEGER_OVERFLOW!>java.lang.Integer.MAX_VALUE + 1<!>,
    p4 = <!INTEGER_OVERFLOW!>java.lang.Integer.MAX_VALUE + 1<!>,
    p5 = java.lang.Integer.MAX_VALUE + 1.toLong(),
    p6 = <!INTEGER_OVERFLOW!>java.lang.Long.MAX_VALUE + 1<!>
) class MyClass

// EXPECTED: @Ann(p1 = 128, p2 = 32768, p3 = -2147483648, p4 = -2147483648, p5 = 2147483648.toLong(), p6 = -9223372036854775808.toLong())
