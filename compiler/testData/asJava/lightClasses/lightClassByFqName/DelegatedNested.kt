// p.B
package p

class B {
    class A(private konst f: I) : I by f {
    }
}

interface I {
    fun g()

    fun f()
}
