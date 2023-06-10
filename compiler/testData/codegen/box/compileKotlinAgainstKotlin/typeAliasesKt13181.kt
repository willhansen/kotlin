// MODULE: lib
// FILE: A.kt
typealias Bar<T> = (T) -> String

class Foo<out T>(konst t: T) {

    fun baz(b: Bar<T>) = b(t)
}

// MODULE: main(lib)
// FILE: B.kt
class FooTest {
    fun baz(): String {
        konst b: Bar<String> = { "OK" }
        return Foo("").baz(b)
    }
}

fun box(): String =
        FooTest().baz()