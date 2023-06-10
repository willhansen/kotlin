annotation class A(konst a: Int, konst c: KClass<*>)

@A(1, Unknown::class)
fun fo<caret>o(): Int = 42