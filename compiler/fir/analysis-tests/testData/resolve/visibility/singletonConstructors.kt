class A {
    companion object Comp {}

    fun foo() {
        <!UNRESOLVED_REFERENCE!>Comp<!>()
    }
}

object B {
    private konst x = <!UNRESOLVED_REFERENCE!>B<!>()
}

class D {
    companion object Comp2 {
        operator fun invoke() {}
    }

    fun foo() {
        Comp2()
    }
}

enum class E {
    X {

    };

    fun foo() {
        <!UNRESOLVED_REFERENCE!>X<!>()
    }
}
