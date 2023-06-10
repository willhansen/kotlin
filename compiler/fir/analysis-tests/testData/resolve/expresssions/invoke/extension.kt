

fun x() {}

class Foo {
    operator fun Int.invoke(): Foo = this@Foo

    konst x = 0

    fun foo() = x() // should resolve to invoke
}
