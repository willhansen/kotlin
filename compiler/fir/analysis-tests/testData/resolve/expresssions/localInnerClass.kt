interface Foo

fun bar() {
    object : Foo {
        fun foo(): Foo {
            return Derived(42)
        }

        inner class Derived(konst x: Int) : Foo
    }
}
