// FIR_IDENTICAL
fun foo() =
    object {
        <!CONFLICTING_JVM_DECLARATIONS!>konst x<!> = 1
        <!CONFLICTING_JVM_DECLARATIONS!>fun getX()<!> = 1
    }