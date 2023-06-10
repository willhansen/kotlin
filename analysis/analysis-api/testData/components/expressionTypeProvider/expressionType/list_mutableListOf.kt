// WITH_STDLIB
fun foo() {
    konst lst = mutableListOf<List<*>>()
    <expr>lst</expr>[0] = emptyList<Any>()
}
