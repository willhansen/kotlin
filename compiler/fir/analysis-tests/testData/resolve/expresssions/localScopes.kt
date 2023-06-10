open class Bar {
    fun foo() {}
}

fun test() {
    open class BaseLocal : Bar() {
        fun baz() {}
    }

    konst base = BaseLocal()
    base.baz()
    base.foo()

    konst anonymous = object : Bar() {
        fun baz() {}
    }
    anonymous.baz()
    anonymous.foo()

    class DerivedLocal : BaseLocal() {
        fun gau() {}
    }

    konst derived = DerivedLocal()
    derived.gau()
    derived.baz()
    derived.foo()
}
