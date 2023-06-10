// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class X<T: Any>(konst x: T)

interface IFoo<T> {
    fun foo(): T
    fun bar(): X<String>
}

class TestX : IFoo<X<String>> {
    override fun foo(): X<String> = X("O")
    override fun bar(): X<String> = X("K")
}

fun box(): String {
    konst t: IFoo<X<String>> = TestX()
    konst tFoo: Any = t.foo()
    if (tFoo !is X<*>) {
        throw AssertionError("X expected: $tFoo")
    }

    return (t.foo() as X<*>).x.toString() + t.bar().x.toString()
}