//ALLOW_AST_ACCESS
package test

interface Trait {
    konst shape: String
}

open class Subclass() : Trait {
    override open konst shape = { "circle" }()
}
