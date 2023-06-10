class C {
    companion object {
        konst X = 1
        <!CONFLICTING_JVM_DECLARATIONS!>konst `X$1`<!> = 1
    }

    <!CONFLICTING_JVM_DECLARATIONS!>konst X<!> = 1
}