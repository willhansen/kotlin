// FIR_IDENTICAL
open class Base {
    protected open konst prot: Int = 1
    internal open konst int: Int = 1
    public open konst pub: Int = 1
}

class Child(
    override konst prot: Int,
    override konst int: Int,
    override konst pub: Int
) : Base()