konst that: Number
    field = 239

fun test() {
    konst a: Number
        <!UNRESOLVED_REFERENCE!>field<!> = 1

    konst b: Number by lazy { 2 }
        <!UNRESOLVED_REFERENCE!>field<!> = 10
}

class A {
    konst c: Number by lazy { 2 }
        <!BACKING_FIELD_FOR_DELEGATED_PROPERTY!>field<!> = 10
}

konst A.cc: Number
    <!EXPLICIT_BACKING_FIELD_IN_EXTENSION!>field<!> = 10

fun A.cc() {
    konst it = <!UNRESOLVED_REFERENCE!>a<!> + 2
}
