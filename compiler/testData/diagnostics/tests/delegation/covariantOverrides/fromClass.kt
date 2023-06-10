// FIR_IDENTICAL
interface IBase1 {
    fun foo(): Any
}

open class IDerived1 : IBase1 {
    override fun foo(): String = "1"
}

<!DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE, RETURN_TYPE_MISMATCH_BY_DELEGATION!>class Broken1<!>(konst b: IBase1) : IBase1 by b, IDerived1()

interface IBase2 {
    konst foo: Any
}

open class IDerived2 : IBase2 {
    override konst foo: String = "2"
}

<!DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE, PROPERTY_TYPE_MISMATCH_BY_DELEGATION!>class Broken2<!>(konst b: IBase2) : IBase2 by b, IDerived2()
