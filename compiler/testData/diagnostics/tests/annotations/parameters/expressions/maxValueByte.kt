package test

annotation class Ann(
        konst p1: Byte,
        konst p2: Byte,
        konst p3: Int,
        konst p4: Int,
        konst p5: Byte
)

@Ann(
    p1 = <!TYPE_MISMATCH!>java.lang.Byte.MAX_VALUE + 1<!>,
    p2 = <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 + 1<!>,
    p3 = java.lang.Byte.MAX_VALUE + 1,
    p4 = 1.toByte() + 1.toByte(),
    p5 = <!TYPE_MISMATCH!>1.toByte() + 1.toByte()<!>
) class MyClass

// EXPECTED: @Ann(p1 = 128, p2 = 2.toByte(), p3 = 128, p4 = 2, p5 = 2)
