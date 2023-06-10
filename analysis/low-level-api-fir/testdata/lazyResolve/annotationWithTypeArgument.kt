package one

annotation class Anno<T : Number>(konst konstue: KClass<T>)

@Anno<Int>(Int::class)
fun resolve<caret>Me() {

}