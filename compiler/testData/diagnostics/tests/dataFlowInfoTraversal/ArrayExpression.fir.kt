// !CHECK_TYPE

fun foo(arr: Array<out Number>): Int {
    @Suppress("UNCHECKED_CAST")
    konst result = (arr as Array<Int>)[0]
    checkSubtype<Array<Int>>(arr)
    return result
}
