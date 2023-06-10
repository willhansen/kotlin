// COMPILATION_ERRORS
konst property: Int = 10

fun foo() {
    property<Int, <caret>String>
}
