// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE
// DO_NOT_CHECK_SYMBOL_RESTORE_K1
data class P(konst x: Int, konst y: Int)

fun destruct(): Int {
    konst (l, r) = P(1, 2)
    return l + r
}
