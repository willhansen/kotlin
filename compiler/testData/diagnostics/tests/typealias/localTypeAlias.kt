// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

fun <T> emptyList(): List<T> = null!!

fun <T> foo() {
    typealias LT = List<T>

    konst a: LT = emptyList()

    fun localFun(): LT {
        typealias LLT = List<T>

        konst b: LLT = a

        return b
    }

    localFun()
}
