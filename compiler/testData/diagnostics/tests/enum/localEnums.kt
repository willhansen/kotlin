// !DIAGNOSTICS: -UNUSED_VARIABLE

fun foo() {
    <!WRONG_MODIFIER_TARGET!>enum<!> class A {
        FOO,
        BAR
    }
    konst foo = A.<!UNINITIALIZED_ENUM_ENTRY!>FOO<!>
    konst b = object {
        <!WRONG_MODIFIER_TARGET!>enum<!> class B {}
    }
    class C {
        <!WRONG_MODIFIER_TARGET!>enum<!> class D {}
    }
    konst f = {
        <!WRONG_MODIFIER_TARGET!>enum<!> class E {}
    }

    <!WRONG_MODIFIER_TARGET!>enum<!> class<!SYNTAX!><!> {}
}
