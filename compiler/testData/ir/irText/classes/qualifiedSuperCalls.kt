// FIR_IDENTICAL
interface ILeft {
    fun foo() {}
    konst bar: Int get() = 1
}

interface IRight {
    fun foo() {}
    konst bar: Int get() = 2
}

class CBoth : ILeft, IRight {
    override fun foo() {
        super<ILeft>.foo()
        super<IRight>.foo()
    }

    override konst bar: Int
        get() = super<ILeft>.bar + super<IRight>.bar
}