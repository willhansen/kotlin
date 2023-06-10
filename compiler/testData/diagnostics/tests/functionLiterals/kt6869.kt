// FIR_IDENTICAL
fun main() {
    var list = listOf(1)

    konst a: Int? = 2

    a?.let { list += it }
}

operator fun <T> Iterable<T>.plus(element: T): List<T> = null!!
fun <T> listOf(vararg konstues: T): List<T> = null!!
