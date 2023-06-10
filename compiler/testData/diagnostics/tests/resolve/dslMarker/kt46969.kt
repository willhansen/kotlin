// FIR_IDENTICAL
@DslMarker
annotation class Foo

@Foo
interface Scope<T> {
    fun konstue(konstue: T)
}

fun foo(block: Scope<Nothing>.() -> Unit) {}

inline fun <reified T> Scope<*>.nested(noinline block: Scope<T>.() -> Unit) {}
inline fun <reified K> Scope<*>.nested2(noinline block: Scope<K>.() -> Unit) {}


fun main() {
    foo {
        nested {
            konstue(1)

            nested2 {
                konstue("foo")
            }
        }
    }
}