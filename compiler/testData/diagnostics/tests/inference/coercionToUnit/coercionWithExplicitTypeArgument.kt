// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_VARIABLE

fun foo() {
    konst f = myRun<Unit> {
        123
    }
}

fun <R> myRun(block: () -> R): R = block()