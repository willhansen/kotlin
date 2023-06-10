class B {
    companion object <!REDECLARATION!>A<!> {
    }

    konst <!REDECLARATION!>A<!> = this
}

class C {
    companion object A {
        konst A = this
    }

}
