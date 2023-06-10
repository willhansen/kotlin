enum class X {
    A
}

annotation class Anno(konst args: A.X)

class B {
    @Anno(X.A)
    fun resolve<caret>Me() {
    }

    @Anno(X.A)
    fun foo() {
    }
}