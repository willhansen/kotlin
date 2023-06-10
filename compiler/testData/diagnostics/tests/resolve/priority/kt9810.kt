// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE
// KT-9810 Local variable vs property from implicit receiver

class A {
    konst foo = 2
}

fun test(foo: String) {
    with(A()) {
        konst g: String = foo // locals win
    }
}