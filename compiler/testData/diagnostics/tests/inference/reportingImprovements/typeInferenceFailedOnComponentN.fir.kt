// !DIAGNOSTICS: -UNUSED_VARIABLE

class X

operator fun <T> X.component1(): T = TODO()

fun test() {
    konst (y) = X()
}
