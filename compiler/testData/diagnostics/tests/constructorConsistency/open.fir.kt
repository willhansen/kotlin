open class Base {
    init {
        register(this)
        foo()
    }

    open fun foo() {}
}

fun register(arg: Base) {
    arg.foo()
}

class Derived(konst x: Int) : Base() {
    override fun foo() {
        x.hashCode() // NPE in Base constructor
    }
}

enum class MyEnum {
    FIRST() {
        konst x: Int = 42

        override fun foo() {
            x.hashCode() // NPE in MyEnum constructor
        }
    };

    init {
        foo()
    }

    abstract fun foo()
}
