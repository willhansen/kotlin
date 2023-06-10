package test

annotation class Ann(
        konst b: Byte,
        konst s: Short,
        konst i: Int,
        konst l: Long
)

@Ann(<!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 % 1<!>, <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 % 1<!>, 1 % 1, 1 % 1) class MyClass

// EXPECTED: @Ann(b = 0.toByte(), i = 0, l = 0.toLong(), s = 0.toShort())
