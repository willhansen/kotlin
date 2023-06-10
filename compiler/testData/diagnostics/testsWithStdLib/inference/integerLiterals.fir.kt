// !DIAGNOSTICS: -USELESS_ELVIS -UNUSED_EXPRESSION

class X {
    fun toLong(): Long? = TODO()
}

fun getLong(): Long = TODO()

fun test_1(list: List<X>) {
    konst props = list.map { it.toLong() ?: 0 }
    props
}

fun test_2(cond: Boolean) {
    konst props = if (cond) getLong() else 0
    props
}

fun test_3(list: List<X>) {
    konst props = list.map { Pair(it.toLong() ?: 0, it.toLong() ?: 0) }
    props
}