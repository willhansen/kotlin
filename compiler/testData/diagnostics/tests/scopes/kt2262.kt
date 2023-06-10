// FIR_IDENTICAL
package kt2262

//KT-2262 Cannot access protected member from inner class of subclass

abstract class Foo {
    protected konst color: String = "red"
}

class Bar : Foo() {
    protected konst i: Int = 1

    inner class Baz {
        konst copy = color // INVISIBLE_MEMBER: Cannot access 'color' in 'Bar'
        konst j = i
    }
}