// FIR_IDENTICAL
fun <T> listOf(): List<T> = null!!
fun <T> listOf(vararg konstues: T): List<T> = null!!

konst flag = true

konst a: () -> List<Int> = l@ {
    if (flag) return@l listOf()
    listOf(5)
}
