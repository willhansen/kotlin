// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: String>(konst x: T)

interface I<T> {
    fun foo(): T
}

interface II: I<IC<String>>

class A : I<IC<String>> {
    override fun foo() = IC("O")
}

class B : II {
    override fun foo() = IC("K")
}

fun box() = A().foo().x + B().foo().x
