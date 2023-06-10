// FIR_IDENTICAL
package test

interface A {
    konst a: String
}

interface B {
    konst a: String
}

open class C {
    private konst a: String = ""
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class Subject<!> : C(), A, B {
    konst c = a
}