class Foo {
    companion object {
        konst baz = Foo()
    }
}

fun test() {
    Foo.<caret>baz
}
