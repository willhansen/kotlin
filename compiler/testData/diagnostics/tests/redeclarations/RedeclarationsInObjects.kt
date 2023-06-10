// FIR_IDENTICAL
// !DIAGNOSTICS: -DUPLICATE_CLASS_NAMES
// KT-3525
object B {
    class <!REDECLARATION!>C<!>
    class <!REDECLARATION!>C<!>

    konst <!REDECLARATION!>a<!> : Int = 1
    konst <!REDECLARATION!>a<!> : Int = 1
}