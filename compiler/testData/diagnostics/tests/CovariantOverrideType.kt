// FIR_IDENTICAL
interface A<H> {
    fun foo() : Int = 1
    fun foo2() : Int = 1
    fun foo1() : Int = 1
    konst a : Int
    konst a1 : Int
    konst g : Iterator<H>

    fun <T> g() : T
    fun <T> g1() : T
}

abstract class B<H>() : A<H> {
    override fun <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>foo<!>() {
    }
    override fun foo2() : <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>Unit<!> {
    }

    override konst a : <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>Double<!> = 1.toDouble()
    override konst <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>a1<!> = 1.toDouble()

    abstract override fun <X> g() : <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>Int<!>
    abstract override fun <X> g1() : <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>List<X><!>

    abstract override konst g : <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>Iterator<Int><!>
}