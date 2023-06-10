// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

class Controller

fun <R> generate(g: suspend Controller.() -> R): R = TODO()

konst test1 = generate {
    3
}