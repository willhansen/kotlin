// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE


fun <T> shuffle(x: List<T>): List<T> = x

fun bar() {
    konst s: (List<String>) -> List<String> = ::shuffle
}