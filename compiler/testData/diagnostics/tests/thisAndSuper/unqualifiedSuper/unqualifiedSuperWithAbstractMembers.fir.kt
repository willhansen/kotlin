// fun foo:     abstract in A,      unresolved in I
// fun bar:     implemented in A,   abstract in I
// fun qux:     abstract in A,      abstract in I
// konst x:       unresolved in A,    abstract in I
// konst y:       abstract in A,      implemented in I

abstract class A {
    abstract fun foo(): Int
    open fun bar() {}
    abstract fun qux()

    abstract konst y: Int
}

interface I {
    fun bar()
    fun qux()

    konst x: Int
    konst y: Int get() = 111
}

class B : A(), I {
    override konst x: Int = 12345
    override konst y: Int = super.y

    override fun foo(): Int {
        super.<!ABSTRACT_SUPER_CALL!>foo<!>()
        return super.<!ABSTRACT_SUPER_CALL!>x<!>
    }

    override fun bar() {
        super.bar()
    }

    override fun qux() {
        <!AMBIGUOUS_SUPER!>super<!>.qux()
    }
}
