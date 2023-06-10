// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

fun <T> emptyList(): List<T> = null!!

fun <T> foo() {
    typealias LT = List<T>

    konst a: <!UNRESOLVED_REFERENCE!>LT<!> = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>emptyList<!>()

    fun localFun(): <!UNRESOLVED_REFERENCE!>LT<!> {
        typealias LLT = List<T>

        konst b: <!UNRESOLVED_REFERENCE!>LLT<!> = a

        return b
    }

    localFun()
}
