fun <T> T.foo(): (a: T) -> Unit = TODO()

fun call() {
    konst x = 123.foo()
    <expr>x(1)</expr>
}
