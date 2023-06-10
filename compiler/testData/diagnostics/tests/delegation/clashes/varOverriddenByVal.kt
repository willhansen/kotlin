// FIR_IDENTICAL
interface IVar {
    var foo: Int
}

interface IDerived : IVar

interface IVal {
    konst foo: Int
}

class CVal : IVal {
    override konst foo: Int get() = 42
}

interface IValT<T> {
    konst foo: T
}

class CValT<T> : IValT<T> {
    override konst foo: T get() = null!!
}

abstract <!VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION!>class Test1<!> : IVar, IVal by CVal()

abstract <!VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION!>class Test2<!> : IVar, IValT<Int> by CValT<Int>()

abstract <!VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION!>class Test3<!> : IDerived, IVal by CVal()

abstract <!VAR_OVERRIDDEN_BY_VAL_BY_DELEGATION!>class Test4<!> : IDerived, IValT<Int> by CValT<Int>()
