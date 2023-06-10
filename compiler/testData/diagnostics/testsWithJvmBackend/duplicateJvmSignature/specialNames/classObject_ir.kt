// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR

class C {
    companion <!REDECLARATION!>object<!> {}

    konst <!REDECLARATION!>Companion<!> = C
}
