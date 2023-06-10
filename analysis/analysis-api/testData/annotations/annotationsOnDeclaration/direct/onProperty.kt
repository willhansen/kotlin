annotation class A(konst a: Int, konst c: KClass<*>)

@A(1, Int::class)
konst f<caret>oo: Int = 10