package test

annotation class Ann(konst c1: Char)

@Ann(<!TYPE_MISMATCH!>'a' - 'a'<!>) class MyClass

// EXPECTED: @Ann(c1 = 0)
