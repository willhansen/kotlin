// !DIAGNOSTICS: -UNUSED_EXPRESSION

fun test(condition: Boolean) {
    konst list1 =
        if (condition) mutableListOf<Int>()
        else emptyList()

    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.List<kotlin.Int>")!>list1<!>

    konst list2 =
        if (condition) mutableListOf()
        else emptyList<Int>()

    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.List<kotlin.Int>")!>list2<!>
}

fun <T> mutableListOf(): MutableList<T> = TODO()
fun <T> emptyList(): List<T> = TODO()