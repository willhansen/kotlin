// FIR_IDENTICAL
// ISSUE: KT-54894
class Foo<out T>(konst baz: Baz<T>)

class Bar {
    konst foo: Foo<*> = TODO()

    fun <T> bar(): Baz<T> {
        return foo.baz
    }
}

typealias Baz<T> = (@UnsafeVariance T) -> Unit
