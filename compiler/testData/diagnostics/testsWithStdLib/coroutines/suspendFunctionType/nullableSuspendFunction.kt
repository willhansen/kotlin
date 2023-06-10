// FIR_IDENTICAL
konst test1: (suspend () -> Unit)? = null
konst test2: <!WRONG_MODIFIER_TARGET!>suspend<!> (() -> Unit)? = null
konst test3: <!WRONG_MODIFIER_TARGET!>suspend<!> ( (() -> Unit)? ) = null

fun foo() {
    test1?.<!ILLEGAL_SUSPEND_FUNCTION_CALL!>invoke<!>()
    test2?.<!ILLEGAL_SUSPEND_FUNCTION_CALL!>invoke<!>()
    test3?.<!ILLEGAL_SUSPEND_FUNCTION_CALL!>invoke<!>()
}