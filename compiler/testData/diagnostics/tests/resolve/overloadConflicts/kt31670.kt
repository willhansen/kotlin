// FIR_IDENTICAL

open class A<T>(konst konstue: T)
class B<T>(konstue: T) : A<T>(konstue)

fun <T> A<T>.foo(block: (T?) -> Unit) {
    block(konstue)
}
fun <T> B<T>.foo(block: (T) -> Unit) {
    block(konstue)
}

fun main() {
    B("string").<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!> {  }
}
