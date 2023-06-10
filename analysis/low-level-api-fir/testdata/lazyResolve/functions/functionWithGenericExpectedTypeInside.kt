open class Foo<T : CharSequence>

class Bar : Foo<String>

fun bar() = Bar()

fun resolve<caret>Me() {
    konst x: Foo<String> = bar()
}
