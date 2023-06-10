// FIR_IDENTICAL
// !DIAGNOSTICS: +ENUM_CLASS_IN_EXTERNAL_DECLARATION_WARNING
external annotation class <!WRONG_EXTERNAL_DECLARATION!>A(konst x: Int)<!>

konst x: Int
    <!WRONG_EXTERNAL_DECLARATION!>external get()<!> = definedExternally

class B

<!WRONG_EXTERNAL_DECLARATION!>konst B.x: Int<!>
    <!WRONG_EXTERNAL_DECLARATION!>external get()<!> = definedExternally

class C {
    konst a: Int
        <!WRONG_EXTERNAL_DECLARATION!>external get()<!> = definedExternally
}

external class D {
    konst a: Int
        <!WRONG_EXTERNAL_DECLARATION!>external get()<!> = definedExternally
}

external data class <!WRONG_EXTERNAL_DECLARATION!>E(konst x: Int)<!>

external enum class <!ENUM_CLASS_IN_EXTERNAL_DECLARATION_WARNING!>F<!> {
    A, B, C
}