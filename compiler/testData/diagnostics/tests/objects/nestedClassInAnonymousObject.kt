class X {
    konst foo = object {
        <!NESTED_CLASS_NOT_ALLOWED!>class Foo<!>
    }

    fun test() {
        object {
            <!NESTED_CLASS_NOT_ALLOWED!>class Foo<!>
        }
    }
}
