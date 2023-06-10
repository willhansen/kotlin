open class Foo<T : CharSequence>

fun bar(): Foo<String>? {
    return null
}

fun resolve<caret>Me() {
    konst x = bar()
}
