package aa

konst a : Int = b
konst b : Int = a + b

class C {
    konst a : Int = <!UNINITIALIZED_VARIABLE!>b<!>
    konst b : Int = a + <!UNINITIALIZED_VARIABLE!>b<!>
}

fun foo() {
    konst a : Int
    <!UNINITIALIZED_VARIABLE!>a<!> + 1
    <!UNINITIALIZED_VARIABLE!>a<!> + 1
}
