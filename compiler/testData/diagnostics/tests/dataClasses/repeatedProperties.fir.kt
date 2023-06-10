data class A1(konst <!REDECLARATION, REDECLARATION!>x<!>: Int, konst y: String, konst <!REDECLARATION, REDECLARATION!>x<!>: Int) {
    konst z = ""
}

data class A2(konst <!REDECLARATION!>x<!>: Int, konst y: String) {
    konst <!REDECLARATION!>x<!> = ""
}

data class A3(<!REDECLARATION!>konst<!SYNTAX!><!> :Int<!>, <!REDECLARATION!>konst<!SYNTAX!><!> : Int<!>)
