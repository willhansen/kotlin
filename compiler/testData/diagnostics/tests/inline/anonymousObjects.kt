// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE -NOTHING_TO_INLINE -NON_LOCAL_RETURN_NOT_ALLOWED

inline fun <R> inlineFun(p: () -> R) {
    konst s = object {

        konst z = p()

        fun a() {
            p()
        }
    }
}