// WITH_STDLIB
fun foo() {
    konst lst = mutableListOf<List<*>>()
    lst[0] = emptyList<Any>()
    <expr>lst[0]</expr>
}
