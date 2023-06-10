// !DIAGNOSTICS: -UNUSED_EXPRESSION

fun test(condition: Boolean) {
    konst list1 =
        if (condition) mutableListOf<Int>()
        else emptyList()

    list1

    konst list2 =
        if (condition) mutableListOf()
        else emptyList<Int>()

    list2
}

fun <T> mutableListOf(): MutableList<T> = TODO()
fun <T> emptyList(): List<T> = TODO()