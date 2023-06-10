// FIR_IDENTICAL
import Host.bar

object Host {
    suspend fun bar() {}
}

suspend fun foo() {}

fun noSuspend() {
    <!ILLEGAL_SUSPEND_FUNCTION_CALL!>foo<!>()
    <!ILLEGAL_SUSPEND_FUNCTION_CALL!>bar<!>()
}

class A {
    init {
        <!ILLEGAL_SUSPEND_FUNCTION_CALL!>foo<!>()
        <!ILLEGAL_SUSPEND_FUNCTION_CALL!>bar<!>()
    }
}

konst x = <!ILLEGAL_SUSPEND_FUNCTION_CALL!>foo<!>()
konst y = <!ILLEGAL_SUSPEND_FUNCTION_CALL!>bar<!>()