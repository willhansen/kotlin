// p.C
// COMPILATION_ERRORS
// FILE: C.kt
package p

class C : A(), I {
    override konst ap: Int
        get() = super.c

    override fun af(): Int {
        return super.foo()
    }

    override konst ip = 5
    override fun if() = 5
}

// FILE: A.kt
package p

abstract class A {
    open internal konst ap: Int = 4
    abstract internal fun af(): Int
}

interface I {
    internal konst ip: Int
    internal fun if(): Int
}
