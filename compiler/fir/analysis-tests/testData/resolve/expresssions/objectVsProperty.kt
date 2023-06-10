object <!REDECLARATION!>A<!>

konst <!REDECLARATION!>A<!> = 10


fun foo() = A

fun bar() {
    konst A = ""
    konst b = A
}

