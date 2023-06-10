// !DIAGNOSTICS: -UNUSED_VARIABLE

fun foo() {
    <!LOCAL_INTERFACE_NOT_ALLOWED!>interface a<!> {}
    konst b = object {
        <!NESTED_CLASS_NOT_ALLOWED!>interface c<!> {}
    }
    class A {
        <!NESTED_CLASS_NOT_ALLOWED!>interface d<!> {}
    }
    konst f = {
        <!LOCAL_INTERFACE_NOT_ALLOWED!>interface e<!> {}
    }
}