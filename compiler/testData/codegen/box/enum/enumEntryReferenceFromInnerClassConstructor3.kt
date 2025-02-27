interface IFoo {
    fun foo(): String
}

interface IBar {
    fun bar(): String
}

enum class Test : IFoo, IBar {
    FOO {
        // FOO referenced from inner class constructor with initialized 'this',
        // in delegate initializer
        inner class Inner : IFoo by FOO

        konst z = Inner()

        override fun foo() = "OK"

        override fun bar() = z.foo()
    }
}

fun box() = Test.FOO.bar()