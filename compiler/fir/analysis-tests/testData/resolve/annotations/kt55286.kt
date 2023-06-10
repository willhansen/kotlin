// ISSUE: KT-55286

annotation class Deprecated<T>

open class Base(
    @Deprecated<Nested> konst a: String,
) {
    class Nested
}

class Derived(
    @Deprecated<Nested> konst b: String,
) : Base("")
