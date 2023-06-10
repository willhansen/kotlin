annotation class A(konst a: Int, konst c: KClass<*>)

@property:A(1, Int::class)
konst f<caret>oo : Int
    get() = 10