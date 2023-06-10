// FIR_IDENTICAL
// !LANGUAGE: +ExpectedTypeFromCast

package pp

class A {
    fun <T> foo(): T = TODO()

    companion object {
        fun <T> foo2(): T = TODO()
    }
}

konst x = A().foo() as String
konst y = A.foo2() as String
konst z = pp.A.foo2() as String
