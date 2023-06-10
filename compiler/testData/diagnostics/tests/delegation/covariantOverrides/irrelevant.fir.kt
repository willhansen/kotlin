interface IBase1 {
    fun foo(): Any
}

interface IDerived1 {
    fun foo(): String
}

<!RETURN_TYPE_MISMATCH_BY_DELEGATION!>class Broken1<!>(konst b: IBase1) : IBase1 by b, IDerived1

interface IBase2 {
    konst foo: Any
}

interface IDerived2 {
    konst foo: String
}

<!PROPERTY_TYPE_MISMATCH_BY_DELEGATION!>class Broken2<!>(konst b: IBase2) : IBase2 by b, IDerived2
