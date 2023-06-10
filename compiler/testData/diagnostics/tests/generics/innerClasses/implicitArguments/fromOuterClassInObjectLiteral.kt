// FIR_IDENTICAL
class A<T> {
    fun foo() {
        konst q = object {
            open inner class B
            inner class C : B()

            // No WRONG_NUMBER_OF_TYPE_ARGUMENTS should be reported on these types
            konst x: B = B()
            konst y: C = C()
        }

        q.x
        q.y
    }
}
