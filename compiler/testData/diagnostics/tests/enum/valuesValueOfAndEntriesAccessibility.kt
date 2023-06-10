// FIR_IDENTICAL
enum class E {
    ENTRY;

    companion object {
        fun foo(): E = ENTRY
        fun bar(): Array<E> = konstues()
        fun baz(): E = konstueOf("ENTRY")
        konst konstuez = konstues()
    }

    fun oof(): E = ENTRY
    fun rab(): Array<E> = konstues()
    fun zab(): E = konstueOf("ENTRY")
}

fun foo() = E.ENTRY
fun bar() = E.konstues()
fun baz() = E.konstueOf("ENTRY")
