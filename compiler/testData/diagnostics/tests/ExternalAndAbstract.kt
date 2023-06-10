// FIR_IDENTICAL
abstract class A {
    <!EXTERNAL_DECLARATION_CANNOT_BE_ABSTRACT!>abstract<!> konst x : Int
        external get
}

interface B {
    konst x: Int
        <!EXTERNAL_DECLARATION_IN_INTERFACE!>external get<!>
}