// FIR_IDENTICAL
// See KT-10061

class My {
    konst x: Int? get() = 42
}

fun foo(my: My) {
    my.x!!
    when (my.x) { }
}