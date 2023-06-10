fun interface A {
    operator fun invoke()
}

konst globalA: A = A {}

fun foo() {
    globalA.invo<caret>ke()
}