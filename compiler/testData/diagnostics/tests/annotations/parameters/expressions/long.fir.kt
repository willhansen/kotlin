package test

annotation class Ann(
        konst l1: Long,
        konst l2: Long,
        konst l3: Long
)

@Ann(1 + 1, java.lang.Long.MAX_VALUE + 1 - 1, java.lang.Long.MAX_VALUE - 1) class MyClass

// EXPECTED: @Ann(l1 = 2.toLong(), l2 = 9223372036854775807.toLong(), l3 = 9223372036854775806.toLong())
