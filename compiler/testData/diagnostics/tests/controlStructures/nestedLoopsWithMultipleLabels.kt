// FIR_IDENTICAL
fun test() {
    a@ b@ while(true) {
        konst f = {
            <!NOT_A_FUNCTION_LABEL!>return@a<!>
        }
        break@b
    }
}
