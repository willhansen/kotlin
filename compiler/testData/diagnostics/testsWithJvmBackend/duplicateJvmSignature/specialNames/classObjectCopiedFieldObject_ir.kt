// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR

class B {
    companion object <!REDECLARATION!>A<!> {
    }

    konst <!REDECLARATION!>A<!>: A = B.A
}

class C {
    companion object A {
        konst A: A = C.A
    }
}

class <!CONFLICTING_JVM_DECLARATIONS!>D<!> {
    companion object A {
        <!CONFLICTING_JVM_DECLARATIONS!>lateinit var A: A<!>
    }
}
