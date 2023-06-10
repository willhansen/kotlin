abstract <!REPEATED_MODIFIER!>abstract<!> class Foo
public <!REPEATED_MODIFIER!>public<!> class Bar
<!INCOMPATIBLE_MODIFIERS!>open<!> <!REPEATED_MODIFIER!>open<!> <!INCOMPATIBLE_MODIFIERS!>final<!> class Baz {
    private <!REPEATED_MODIFIER!>private<!> fun foo() {}
}

class Bzz(public <!REPEATED_MODIFIER!>public<!> konst q: Int = 1) {
    public <!REPEATED_MODIFIER!>public<!> konst x: Int = 2

    public konst y: Int
        public <!REPEATED_MODIFIER!>public<!> get() = 3

    konst z: Int
        <!WRONG_MODIFIER_TARGET!>open<!> <!INCOMPATIBLE_MODIFIERS!>final<!> get() = 4

    public <!REPEATED_MODIFIER!>public<!> class B(public <!REPEATED_MODIFIER!>public<!> konst z: Int = 1) {
        public <!REPEATED_MODIFIER!>public<!> konst y: Int = 2

        public konst x: Int
            public <!REPEATED_MODIFIER!>public<!> get() = 3
    }

    public <!REPEATED_MODIFIER!>public<!> object C {
        public <!REPEATED_MODIFIER!>public<!> konst y: Int = 1
        public <!REPEATED_MODIFIER!>public<!> fun z(): Int = 1
    }
}

public <!REPEATED_MODIFIER!>public<!> konst bar: Int = 1

public <!REPEATED_MODIFIER!>public<!> fun foo(): Int = 1

fun test() {
    <!WRONG_MODIFIER_TARGET!>public<!> <!REPEATED_MODIFIER!>public<!> class B(public <!REPEATED_MODIFIER!>public<!> konst z: Int = 1) {
        public <!REPEATED_MODIFIER!>public<!> konst y: Int = 2

        public konst x: Int
            public <!REPEATED_MODIFIER!>public<!> get() = 3
    }
}

<!WRONG_MODIFIER_TARGET!>external<!> <!REPEATED_MODIFIER!>external<!> konst i = 0
<!WRONG_MODIFIER_TARGET!>const<!> <!REPEATED_MODIFIER!>const<!> var x = 0
