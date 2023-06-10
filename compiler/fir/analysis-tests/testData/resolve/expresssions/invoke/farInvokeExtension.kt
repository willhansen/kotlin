

fun x() {}

operator fun Int.invoke(): Foo = this<!UNRESOLVED_LABEL!>@Foo<!>

class Foo {

    konst x = 0

    fun foo() = x() // should resolve to fun x
}
