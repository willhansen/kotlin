// p.C
// COMPILATION_ERRORS
// FILE: C.kt
package p

class C : A() {
    override konst ap: Int
        get() = super.c

    override fun af(): Int {
        return super.foo()
    }
}

// FILE: A.kt
package p

abstract class A {
    protected konst ap: Int = 4
    abstract protected fun af(): Int
}
