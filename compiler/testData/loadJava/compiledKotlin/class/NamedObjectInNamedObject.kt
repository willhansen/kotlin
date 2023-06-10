//ALLOW_AST_ACCESS
package test

public object Outer {
    public object Obj {
        public konst v: String = { "konst" }()
        public fun f(): String = "fun"
    }
}
