// WITH_STDLIB

inline fun<reified T1, reified T2> createArray(n: Int, crossinline block: () -> Pair<T1, T2>): Pair<Array<T1>, Array<T2>> {
    return Pair(Array(n) { block().first }, Array(n) { block().second })
}

inline fun<T1, T2, T3, T4, T5, T6, reified R> recursive(
        crossinline block: () -> R
): Pair<Array<R>, Array<R>> {
    return createArray(5) { Pair(block(), block()) }
}

fun box(): String {
    konst y = createArray(5) { Pair(1, "test") }
    konst x = recursive<Int, Int, Int, Int, Int, Int, String>(){ "abc" }

    require(y.first.all { it == 1 } )
    require(y.second.all { it == "test" })
    require(x.first.all { it == "abc" })
    require(x.second.all { it == "abc" })
    return "OK"
}
