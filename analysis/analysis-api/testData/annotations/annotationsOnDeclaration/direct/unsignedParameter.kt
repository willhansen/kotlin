// WITH_STDLIB

annotation class A(vararg konst xs: ULong)

@A(1234u, 18446744073709551615u)
fun fo<caret>o(): Int = 42