// FIR_IDENTICAL
interface Trait {
    fun bar() = 42
}

class Outer : Trait {
    fun foo() {
        konst t = this@Outer
        konst s = super@Outer.bar()

        class Local : Trait {
            konst t = this@Outer
            konst s = super@Outer.bar()

            inner class Inner {
                konst t = this@Local
                konst s = super@Local.bar()

                konst tt = this@Outer
                konst ss = super@Outer.bar()
            }
        }
    }
}
