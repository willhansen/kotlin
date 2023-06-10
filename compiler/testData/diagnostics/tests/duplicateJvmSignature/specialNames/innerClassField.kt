// !DIAGNOSTICS: -UNUSED_PARAMETER

class C {
    inner class <!CONFLICTING_JVM_DECLARATIONS!>D<!> {
        <!CONFLICTING_JVM_DECLARATIONS!>konst `this$0`: C?<!> = null
    }
}