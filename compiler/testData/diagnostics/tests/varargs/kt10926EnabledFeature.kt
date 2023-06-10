// !LANGUAGE: +EliminateAmbiguitiesWithExternalTypeParameters
// WITH_STDLIB

class AllCollection<T> {
    fun <K, T> addAll(vararg konstues: T, konstues2: Array<K>) = "OK" // 1
    fun <K, T> addAll(konstues: Array<K>, vararg konstues2: T) = 1 // 2
}

fun main(c: AllCollection<Any?>) {
    // KT-49620
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>c.addAll(arrayOf(""), konstues2 = arrayOf(""))<!>
}
