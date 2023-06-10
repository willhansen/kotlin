sealed class Foo {
    object A : Foo()
    class B(konst i: Int) : Foo()
}

fun test(e: Foo) {
    <caret>when (e) {
        else -> {}
    }
}
