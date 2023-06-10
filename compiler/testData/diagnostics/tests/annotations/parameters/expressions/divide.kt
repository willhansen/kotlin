package test

annotation class Ann(
        konst b: Byte,
        konst s: Short,
        konst i: Int,
        konst l: Long
)

@Ann(<!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 / 1<!>, <!INTEGER_OPERATOR_RESOLVE_WILL_CHANGE!>1 / 1<!>, 1 / 1, 1 / 1) class MyClass

// EXPECTED: @Ann(b = 1.toByte(), i = 1, l = 1.toLong(), s = 1.toShort())
