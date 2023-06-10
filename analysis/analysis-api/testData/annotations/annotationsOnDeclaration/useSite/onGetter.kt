annotation class A(konst a: Int, konst c: KClass<*>)

@get:A(1, Int::class)
konst foo : Int
    ge<caret>t() = 10