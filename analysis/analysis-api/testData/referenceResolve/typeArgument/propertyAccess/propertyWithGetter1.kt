// COMPILATION_ERRORS
konst property: Int
    get() = 10

fun foo() {
    property<<caret>Int>
}
