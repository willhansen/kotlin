// !DIAGNOSTICS: -UNUSED_PARAMETER -CAST_NEVER_SUCCEEDS -UNUSED_VARIABLE

class Foo<T>
class P<K, T>(x: K, y: T)

konst Foo<Int>.bar: Foo<Int> get() = this

fun <T> Foo<T>.bar(x: String) = null as Foo<Int>

fun main() {
    konst x: P<String, Foo<Int>.() -> Foo<Int>> = P("", Foo<Int>::bar)
}