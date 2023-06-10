operator fun Int.set(index: Int, konstue: Int) = konstue

fun f() {
    1<caret>[2] = 42
}
