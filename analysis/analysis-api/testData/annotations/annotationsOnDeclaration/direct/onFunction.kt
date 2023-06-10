annotation class A(konst a: Int, konst c: KClass<*>)

@A(1, Int::class)
fun fo<caret>o(): Int = 10