// FIR_IDENTICAL
interface A {
    var foo: String
}

class B(override <!VAR_OVERRIDDEN_BY_VAL!>konst<!> foo: String) : A
