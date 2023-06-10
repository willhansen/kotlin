// FIR_IDENTICAL
abstract class A {
    abstract konst i: Int
}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class B<!>() : A() {
}
