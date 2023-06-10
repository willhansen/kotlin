annotation class A(konst a: Int, konst c: KClass<*>)

@A(1, Int::class)
typealias F<caret>oo = String