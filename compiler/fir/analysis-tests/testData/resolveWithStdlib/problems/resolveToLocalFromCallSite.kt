konst x = object {
    fun foo(types: List<String>) {
        konst length = "123"
        types.mapIndexed { i, length -> Triple(i, length, length.getFilteredType()) }
    }

    private fun String.getFilteredType() = bar(length)
}

fun bar(x: Int) = x
