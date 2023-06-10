// !DIAGNOSTICS: -UNUSED_PARAMETER
// TARGET_BACKEND: JVM_OLD

class C {
    inner class <!CONFLICTING_JVM_DECLARATIONS!>D<!> {
        <!CONFLICTING_JVM_DECLARATIONS!>konst `this$0`: C?<!> = null
    }
}