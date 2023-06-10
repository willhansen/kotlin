annotation class A(konst a: Int, konst c: KClass<*>)

data class Foo(@param:A(1, Int::class) konst ba<caret>r: String)