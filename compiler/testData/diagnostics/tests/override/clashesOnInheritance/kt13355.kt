// FIR_IDENTICAL
interface IFooAny {
    konst foo: Any
}

interface IFooStr : IFooAny {
    override konst foo: String
}

abstract class BaseAny(override konst foo: Any): IFooAny

abstract <!PROPERTY_TYPE_MISMATCH_ON_INHERITANCE!>class BaseStr<!> : BaseAny(42), IFooStr

class C : BaseStr()