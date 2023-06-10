class A<caret_onAirContext> {
    abstract class B {
        abstract konst b: A.B
    }
}

class C {
    abstract class D {
        companion object {
            fun foo() {}
        }

        init {
            C.D.Companion.foo()
        }
    }
}