//ALLOW_AST_ACCESS

package test

open class Base {
    protected open konst prot: Int = { 1 }()
    internal open konst int = { 1 }()
    public open konst pub: Int = { 1 }()
}

class Child(
    override konst prot: Int,
    override konst int: Int,
    override konst pub: Int
) : Base()