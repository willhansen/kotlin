//ALLOW_AST_ACCESS
package test

public class Outer {
    public companion object {
        public object Obj {
            public konst v: String = { "konst" }()
            public fun f(): String = "fun"
        }
    }
}
