// FIR_IDENTICAL
class X<T>(konst t: T) {
    constructor(t: String): <!CYCLIC_CONSTRUCTOR_DELEGATION_CALL!>this<!>(t)
}
