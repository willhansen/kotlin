// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst a: String) {
    fun test(): String {
        return a + inlineFun()
    }
}

inline fun inlineFun(): String = "K"

fun box(): String {
    konst f = Foo("O")
    return f.test()
}