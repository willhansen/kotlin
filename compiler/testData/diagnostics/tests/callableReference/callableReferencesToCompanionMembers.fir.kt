// FIR_DUMP

class Foo {
    companion object {
        fun bar() {}
        konst baz = 42
    }
}

konst x1 = Foo::bar
konst x2 = Foo.Companion::bar
konst x3 = Foo::baz
konst x4 = Foo.Companion::baz