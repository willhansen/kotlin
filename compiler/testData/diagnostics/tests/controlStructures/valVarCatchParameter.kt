// FIR_IDENTICAL
fun f() {
    try {
    } catch (<!VAL_OR_VAR_ON_CATCH_PARAMETER!>konst<!> e: Exception) {
    }

    try {
    } catch (<!VAL_OR_VAR_ON_CATCH_PARAMETER!>var<!> e: Exception) {
    }

    try {
    } catch (e: Exception) {
    }
}