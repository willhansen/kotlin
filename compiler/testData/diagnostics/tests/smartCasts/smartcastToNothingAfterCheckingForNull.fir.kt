// !DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: B.java

public abstract class B<T> implements A<T> {}

// FILE: test.kt

interface A<T> {
    konst content: T
}
fun f(x: Any?) {}
fun f(x: Byte) {}
fun f(x: Char) {}

fun g(i: Int) {}

fun g(x: B<Int>) {
    konst y = x.content
    if (y == null) {
        f(y)
        <!NONE_APPLICABLE!>g<!>(y)
    }

    if (y is Nothing?) {
        f(y)
        <!NONE_APPLICABLE!>g<!>(y)
    }
}
