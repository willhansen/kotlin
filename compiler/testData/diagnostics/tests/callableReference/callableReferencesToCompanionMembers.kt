// FIR_DUMP

class Foo {
    companion object {
        fun bar() {}
        konst baz = 42
    }
}

konst x1 = <!INCORRECT_CALLABLE_REFERENCE_RESOLUTION_FOR_COMPANION_LHS!>Foo::bar<!>
konst x2 = Foo.Companion::bar
konst x3 = <!INCORRECT_CALLABLE_REFERENCE_RESOLUTION_FOR_COMPANION_LHS!>Foo::baz<!>
konst x4 = Foo.Companion::baz
