interface T {
    fun foo()
}

open class C {
    protected fun foo() {}
}

class E : C(), T

konst z: T = object : C(), T {}
