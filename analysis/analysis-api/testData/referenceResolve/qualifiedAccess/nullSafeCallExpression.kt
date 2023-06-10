class A(konst aa: A?)

fun f(a: A) {
    konst x = a?.a<caret>a
}