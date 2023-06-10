//ALLOW_AST_ACCESS
package test

open class BaseClass() {
    open konst shape = { "square" }()
}

open class Subclass() : BaseClass() {
    override open konst shape = { "circle" }()
}
