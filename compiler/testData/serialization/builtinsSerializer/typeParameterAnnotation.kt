package test

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class Ann(konst konstue: String)
inline fun <reified @Ann("abc") T> foo() {}
