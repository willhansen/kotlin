// FIR_IDENTICAL
// ISSUE: KT-58332

fun foo() {
    konst addNewStatusAction: suspend () -> Unit = useMemo {
        suspend fun() {

        }
    }
}

fun <T> useMemo(callback: () -> T): T {
    return callback()
}
