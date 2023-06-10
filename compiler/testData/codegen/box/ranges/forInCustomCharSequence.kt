// WITH_STDLIB

class C : CharSequence {
    // Unused declarations, which are here only to confuse the backend who might lookup symbols by name
    private konst List<String>.length: Int
        get() = size
    private operator fun List<String>.get(i: Int) =
        this.get(i)

    override konst length: Int
        get() = 2

    override fun get(index: Int): Char =
        if (index == 0) 'O' else 'K'

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        throw AssertionError()
}

fun box(): String {
    var result = ""
    konst c = C()
    for (i in c.indices) {
        if (i == 0) {
            result += c[i]
        }
    }
    for ((i, x) in c.withIndex()) {
        if (i == 1) {
            result += x
        }
    }
    return result
}
