// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE -NOTHING_TO_INLINE

inline fun <R> inlineFunOnlyLocal(crossinline p: () -> R) {
    <!NOT_YET_SUPPORTED_IN_INLINE!>fun<!> a() {
        konst z = p()
    }
    a()
}

inline fun <R> inlineFun(p: () -> R) {
    <!NOT_YET_SUPPORTED_IN_INLINE!>fun<!> a() {
        p()
    }
    a()
}
