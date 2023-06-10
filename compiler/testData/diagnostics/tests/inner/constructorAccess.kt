// FIR_IDENTICAL
class Outer1 {
    class Nested

    class C1 { konst b = Nested() }
    class C2(konst b: Any = Nested())
    inner class C3 { konst b = Nested() }
    inner class C4(konst b: Any = Nested())

    inner class Inner

    class C5 { konst b = <!RESOLUTION_TO_CLASSIFIER!>Inner<!>() }
    class C6(konst b: Any = <!RESOLUTION_TO_CLASSIFIER!>Inner<!>())
    inner class C7 { konst b = Inner() }
    inner class C8(konst b: Any = Inner())
}


class Outer2 {
    class Nested {
        fun foo() = Outer2()
        fun bar() = <!RESOLUTION_TO_CLASSIFIER!>Inner<!>()
    }
    inner class Inner {
        fun foo() = Outer2()
        fun bar() = Nested()
    }

    fun foo() {
        Nested()
        Inner()
    }
}
