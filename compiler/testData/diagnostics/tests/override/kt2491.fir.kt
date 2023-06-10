interface T {
    public fun foo()
}

open class C {
    protected fun foo() {}
}

class D : C(), T

konst obj: C = object : C(), T {}