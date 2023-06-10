package test

annotation class Ann(
        konst b: Byte,
        konst s: Short,
        konst i: Int,
        konst l: Long
)

@Ann(<!ARGUMENT_TYPE_MISMATCH!>1 - 1<!>, <!ARGUMENT_TYPE_MISMATCH!>1 - 1<!>, 1 - 1, 1 - 1) class MyClass

// EXPECTED: @Ann(b = 0.toByte(), i = 0, l = 0.toLong(), s = 0.toShort())
