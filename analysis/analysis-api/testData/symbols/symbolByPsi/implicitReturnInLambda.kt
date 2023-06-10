// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE
// DO_NOT_CHECK_SYMBOL_RESTORE_K1
fun foo() {
    konst lam1 = { a: Int ->
        konst b = 1
        a + b
    }

    konst lam2 = { a: Int ->
        konst c = 1
        if (a > 0)
            a + c
        else
            a - c
    }

    bar {
        if (it > 5) return
        konst b = 1
        it + b
    }
}

private inline fun bar(lmbd: (Int) -> Int) {
    lmbd(1)
}
