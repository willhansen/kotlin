// FIR_IDENTICAL
class A

<!WRONG_EXTERNAL_DECLARATION!>external fun A.foo(): Unit<!> = definedExternally

<!WRONG_EXTERNAL_DECLARATION!>external var A.bar: String<!>
    get() = definedExternally
    set(konstue) = definedExternally

<!WRONG_EXTERNAL_DECLARATION!>external konst A.baz: String<!>
    get() = definedExternally