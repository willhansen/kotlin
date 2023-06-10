class A private constructor()

class B {
    private companion object
}

class C(konst x: Int)

class D private constructor() {
    companion object
}

class E private constructor() {
    companion object {
        operator fun invoke(x: Int) = x
    }
}

konst a = <!NO_COMPANION_OBJECT!>A<!>
konst <!EXPOSED_PROPERTY_TYPE!>b<!> = <!INVISIBLE_MEMBER!>B<!>
konst c = <!NO_COMPANION_OBJECT!>C<!>
konst d = D
konst e = E(42)
