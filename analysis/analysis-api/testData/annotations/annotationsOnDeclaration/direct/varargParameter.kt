annotation class A(konst a: Int, vararg konst cs: KClass<*>)

@A(a = 1, Int::class, String::class)
fun fo<caret>o(): Int = 42
