// FIR_IDENTICAL
konst <!REDECLARATION!>a<!> : Int = 1
konst <!REDECLARATION!>a<!> : Int = 1
konst <!REDECLARATION!>a<!> : Int = 1

konst <!REDECLARATION!>b<!> : Int = 1
konst <!REDECLARATION!>b<!> : Int = 1
konst <!REDECLARATION!>b<!> : Int = 1
konst <!REDECLARATION!>b<!> : Int = 1

<!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here too
<!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here
<!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here
<!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here

<!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here
<!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here
<!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here

class A {
    konst <!REDECLARATION!>a<!> : Int = 1
    konst <!REDECLARATION!>a<!> : Int = 1
    konst <!REDECLARATION!>a<!> : Int = 1

    konst <!REDECLARATION!>b<!> : Int = 1
    konst <!REDECLARATION!>b<!> : Int = 1
    konst <!REDECLARATION!>b<!> : Int = 1
    konst <!REDECLARATION!>b<!> : Int = 1

    <!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here too
    <!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here
    <!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here
    <!CONFLICTING_OVERLOADS!>fun foo()<!> {} // and here

    <!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here
    <!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here
    <!CONFLICTING_OVERLOADS!>fun bar()<!> {} // and here
}
