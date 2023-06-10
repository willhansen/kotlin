// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo {
    fun getO(): String
    konst k: String

    konst ok: String get() = getO() + k
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineFooImpl<T: String>(konst s: T): IFoo {
    override fun getO(): String = s
    override konst k: String get() = "K"
}

class Test(s: String) : IFoo by InlineFooImpl(s)

fun box() = Test("O").ok