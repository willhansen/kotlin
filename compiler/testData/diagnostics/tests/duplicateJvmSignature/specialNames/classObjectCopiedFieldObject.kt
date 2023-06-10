class B {
    companion object <!REDECLARATION!>A<!> {
    }

    konst <!REDECLARATION!>A<!> = <!DEBUG_INFO_LEAKING_THIS!>this<!>
}

class C {
    companion <!CONFLICTING_JVM_DECLARATIONS!>object A<!> {
        <!CONFLICTING_JVM_DECLARATIONS!>konst A<!> = <!DEBUG_INFO_LEAKING_THIS!>this<!>
    }

}
