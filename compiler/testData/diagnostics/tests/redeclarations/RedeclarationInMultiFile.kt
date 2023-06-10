// FIR_IDENTICAL
// FILE: a.kt
konst <!REDECLARATION!>a<!> : Int = 1
<!CONFLICTING_OVERLOADS!>fun f()<!> {
}

// FILE: b.kt
konst <!REDECLARATION!>a<!> : Int = 1
<!CONFLICTING_OVERLOADS!>fun f()<!> {
}
