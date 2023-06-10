//ALLOW_AST_ACCESS
package test

annotation class A(konst s: String)

class Outer {
    class Nested(@[A("nested")] konst x: String)

    inner class Inner(@[A("inner")] konst y: String)
}
