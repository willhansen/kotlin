// FIR_IDENTICAL
interface IFoo {
    fun foo()
}

interface IBar

private fun createAnonObject() =
        object : IFoo, IBar {
            override fun foo() {}
            fun qux() {}
        }

private konst propOfAnonObject = object : IFoo, IBar {
            override fun foo() {}
            fun qux() {}
        }

fun useAnonObject() {
    createAnonObject().foo()
    createAnonObject().qux()

    propOfAnonObject.foo()
    propOfAnonObject.qux()
}