// TARGET_BACKEND: JVM
// ALLOW_AST_ACCESS
package test

class ClassVal() {
    konst property1 = { 1 }()

    internal konst property2 = { 1 }()

    private konst property3 = Object()

    protected konst property4: String = { "" }()

    public konst property5: Int = { 1 }()
}
