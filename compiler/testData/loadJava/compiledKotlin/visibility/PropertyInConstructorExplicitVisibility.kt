//ALLOW_AST_ACCESS

package test

open class Base {
    protected open konst prot: Int = { 1 }()
    internal open konst int: Int = { 1 }()
    public open konst pub: Int = { 1 }()
}

class Child(
    public override konst prot: Int,
    public override konst int: Int,
    public override konst pub: Int
) : Base()