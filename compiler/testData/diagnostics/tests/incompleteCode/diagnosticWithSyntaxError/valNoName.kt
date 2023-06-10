// FIR_IDENTICAL
// VAL
class A(
        konst<!SYNTAX!><!>
        konst x: Int,
        konst
        private<!SYNTAX!><!> konst z: Int,
        konst<!SYNTAX!><!>
)

konst<!SYNTAX!><!>
fun foo() {}

class B {
    konst<!SYNTAX!><!>
    fun foo() {}

    fun bar() {
        konst<!SYNTAX!><!>
        fun foo() {}
    }
}

// VAR
class C(
        var<!SYNTAX!><!>
        konst x: Int,
        var
        private<!SYNTAX!><!> konst z: Int,
        var<!SYNTAX!><!>
)

var<!SYNTAX!><!>
fun baz() {}

class D {
    var<!SYNTAX!><!>
    fun foo() {}

    fun bar() {
        var<!SYNTAX!><!>
        fun foo() {}
    }
}
